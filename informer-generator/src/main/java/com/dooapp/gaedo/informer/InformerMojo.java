package com.dooapp.gaedo.informer;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.visitor.VoidVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Named;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;


/**
 * Goal generating informer classes in a target folder from bean classes.
 * 
 * @author ndx
 * @goal generate-informers
 * @phase generate-sources
 */
@Named("informer")
public class InformerMojo extends AbstractMojo implements Serializable {
	/**
	 * Output file where those classes will be written
	 * 
	 * @parameter default-value="${project.build.directory}/generated/informers"
	 */
	private File output;

	/**
	 * Output file where those classes will be written
	 * 
	 * @parameter default-value="${project.build.directory}/classes/META-INF/informer-mappings.properties"
	 */
	private File informerMappings;
	
	/**
	 * When {@link #generateMappings} is true, this field will contain the mappings loaded from (and later writen to) {@link #informerMappings}
	 */
	private Properties currentInformerMappings;
	
	/**
	 * Flag indicating if informer mappings file should be generated
	 * 
	 * @parameter default-value="false"
	 */
	private boolean generateMappings;
	/**
	 * Maven project object, used to get sources paths
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	public MavenProject project;
	/**
	 * Sources that will be scanned
	 * 
	 * @parameter
	 * @category file-scanning
	 */
	public String[] includes = { "**/*.java" };

	/**
	 * Defines which of the included files in the source directories to exclude
	 * (none by default).
	 * 
	 * @parameter
	 * @category file-scanning
	 */
	public String[] excludes = new String[0];

	/**
	 * Only classes implementing those interfaces will have their informers
	 * generated. Interface name should be fully qualified. Notice that this
	 * array is by default empty, which make it require no interface at all.
	 * 
	 * @parameter
	 * @category file-parsing
	 */
	public String[] requiredInterfaces = new String[0];

	/**
	 * Only classes having those annotations defined wil have their informers
	 * generated Annotation name should be fully qualified.
	 * 
	 * @parameter
	 * @category file-parsing
	 */
	public String[] requiredAnnotations = new String[0];

	/**
	 * A list of qualified names of enums containing the enums not in this project source path, but required to be enums.
	 * @parameter
	 */
	private String[] enums = new String[0];
	
	/**
	 * A list of properties names that should be excluded. Format of properties written here can be in two forms:
	 * <ul>
	 * <li>Simple strings</li>
	 * <li>Javadoc-comaptible properties : package.class#property</li>
	 * </ul>
	 * @parameter
	 */
	public String[] propertiesExcludes = new String[0];
	
	/**
	 * Define if informers should be generated for static fields. As it's not really interesting to have such a behaviour, it is disabled as default.
	 * @parameter default-value="false"
	 */
	private boolean includeStaticProperties = false;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(generateMappings) {
			currentInformerMappings = new Properties();
			if(informerMappings.exists()) {
				try {
					currentInformerMappings.load(new FileInputStream(informerMappings));
				} catch (Exception e) {
					throw new MojoExecutionException("unable to read current informer mappings from "+informerMappings.getAbsolutePath(), e);
				}
			}
		}
		getLog().info("examining all classes looking for the beans ones");
		Map<String, Class> resolvedInformers = com.dooapp.gaedo.informer.Utils.createResolvedInformers();
		Collection<File> sources = getSourceFiles(project.getCompileSourceRoots());
		Collection<String> qualifiedEnums = findQualifiedEnums(sources);
		qualifiedEnums.addAll(Arrays.asList(enums));
		getLog().info("there seems to be those enums in the path : "+qualifiedEnums);
		Collection<CompilationUnit> matching = findMatchingSources(sources);
		// now generate informer for each interesting file (but for that we have
		// to know if file is interesting
		Collection<String> excludedProperties = new LinkedList<String>();
		excludedProperties.addAll(Arrays.asList(propertiesExcludes));
		try {
			for (CompilationUnit u : matching) {
				buildInformerFor(u, qualifiedEnums, resolvedInformers, excludedProperties);
			}
			// Finally add generated sources to sources for project
			project.getCompileSourceRoots().add(output.getCanonicalPath());
		} catch (IOException e) {
			throw new MojoExecutionException("unable to resolve output folder to a canonical path", e);
		}
		if(generateMappings) {
			informerMappings.getParentFile().mkdirs();
			try {
				currentInformerMappings.store(new FileOutputStream(informerMappings), "informer mappings generated by Gaedo Informer generator mojo");
				getLog().info("written informer mappings in "+informerMappings.getAbsolutePath());
			} catch (Exception e) {
				throw new MojoExecutionException("unable to write current informer mappings to "+informerMappings.getAbsolutePath(), e);
			}
		}
	}

	public Collection<String> findQualifiedEnums(Collection<File> sources) {
		Collection<String> returned = new LinkedList<String>();
		if(enums!=null) {
			returned.addAll(Arrays.asList(enums));
		}
		for (File f : sources) {
			try {
				CompilationUnit searched = JavaParser.parse(f);
				EnumScanner enums = new EnumScanner();
				enums.visit(searched, null);
				if (enums.getQualifiedName()!=null) {
					returned.add(enums.getQualifiedName());
				}
			} catch (Exception e) {
				getLog().error("file " + f + " couldn't be parsed", e);
			}
		}
		return returned;
	}

	/**
	 * Build informer for the given compilation unit
	 * @param source source compilation unit, from which informations will be read
	 * @param qualifiedEnums known qualified enums in project
	 * @param resolvedInformers already resolved informers
	 * @param excludedProperties collection of excluded properties definitions
	 * @throws IOException
	 */
	public void buildInformerFor(CompilationUnit source, Collection<String> qualifiedEnums, Map<String, Class> resolvedInformers, Collection<String> excludedProperties) throws IOException {
		createInformerFileFrom(extractInformerOf(source, includeStaticProperties, excludedProperties), output, qualifiedEnums, resolvedInformers);
	}

	/**
	 * Generate the informer file from the obtained informations
	 * @param informerInfos informations extracted from a compilation unit
	 * @param baseOutput base output dir, from which package and class name will be both extracted
	 * @param qualifiedEnums already known qualified enums
	 * @param resolvedInformers resolved informers
	 * @throws IOException
	 */
	public void createInformerFileFrom(InformerInfos informerInfos, File baseOutput, Collection<String> qualifiedEnums, Map<String, Class> resolvedInformers) throws IOException {
		getLog().info("generating informer for "+informerInfos.classPackage+"."+informerInfos.className);
		getLog().debug("it should have as FieldInformers "+informerInfos.properties);
		CompilationUnit cu = InformerTextGenerator.generateCompilationUnit(informerInfos, qualifiedEnums, resolvedInformers);
		File effective = new File(baseOutput+"/"+informerInfos.classPackage.replace('.', '/')+"/"+informerInfos.getInformerName()+".java");
		effective.getParentFile().mkdirs();
		FileUtils.fileWrite(effective, cu.toString());
		if(generateMappings) {
			currentInformerMappings.setProperty(informerInfos.getQualifiedClassName(), informerInfos.getQualifiedInformerName());
		}
	}

	/**
	 * Extract all properties of given compilation unit
	 * 
	 * @param u
	 * @param includeStaticProperties see {@link #includeStaticProperties} for definition
	 * @param excludedProperties collection of excluded properties
	 * @return collection of extracted properties
	 */
	public InformerInfos extractInformerOf(CompilationUnit u, boolean includeStaticProperties, Collection<String> excludedProperties) {
		return new ScanningPropertiesBuilder(includeStaticProperties, excludedProperties).build(u);
	}

	/**
	 * Extract from the whole collection of files representing java sources the
	 * smaller collection of copmilation units representing classes matching
	 * with either {@link #requiredAnnotations} or {@link #requiredInterfaces}
	 * 
	 * @param sources
	 * @return
	 */
	public List<CompilationUnit> findMatchingSources(Collection<File> sources) {
		List<CompilationUnit> returned = new LinkedList<CompilationUnit>();
		for (File f : sources) {
			try {
				CompilationUnit searched = JavaParser.parse(f);
				ScanningMatcher interfaces = new RequiredInterfacesScanner(requiredInterfaces);
				ScanningMatcher annotations = new RequiredAnnotationsScanner(requiredAnnotations);
				if (interfaces.matches(searched) && annotations.matches(searched)) {
					returned.add(searched);
				}
			} catch (Exception e) {
				getLog().error("file " + f + " couldn't be parsed", e);
			}
		}
		return returned;
	}

	/**
	 * Get all source files matching criterias. These files all match
	 * {@link #includes} filter while non matching {@link #excludes}
	 * 
	 * @param compileSourceRoots
	 * @return a collection of files representing effective java classes
	 * @throws MojoExecutionException
	 */
	public Collection<File> getSourceFiles(List<String> compileSourceRoots) throws MojoExecutionException {
		Collection<File> matching = new LinkedList<File>();
		for (String root : compileSourceRoots) {
			getLog().debug("examining " + root);
			matching.addAll(scan(new File(root)));
		}
		return matching;
	}

	/**
	 * Scans a single directory.
	 * 
	 * @param root
	 *            Directory to scan
	 * @param writer
	 *            Where to write the source list
	 * @throws MojoExecutionException
	 *             in case of IO errors
	 * @return collection of files matching criterias
	 */
	private Collection<File> scan(File root) throws MojoExecutionException {
		Collection<File> returned = new LinkedList<File>();

		if (!root.exists()) {
			getLog().warn(root + " is not an existing directory");
			return returned;
		}

		getLog().info("scanning source file directory '" + root + "'");

		final DirectoryScanner directoryScanner = new DirectoryScanner();
		directoryScanner.setIncludes(includes);
		directoryScanner.setExcludes(excludes);
		directoryScanner.setBasedir(root);
		directoryScanner.scan();

		for (String fileName : directoryScanner.getIncludedFiles()) {
			File file = new File(root, fileName);
			returned.add(file);
		}
		return returned;
	}

	/**
	 * @return the output
	 * @category getter
	 * @category output
	 */
	public File getOutput() {
		return output;
	}

	/**
	 * @param output
	 *            the output to set
	 * @category setter
	 * @category output
	 */
	public void setOutput(File output) {
		this.output = output;
	}
}

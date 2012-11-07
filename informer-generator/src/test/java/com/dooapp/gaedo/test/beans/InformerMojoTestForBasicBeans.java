package com.dooapp.gaedo.test.beans;

import static com.dooapp.gaedo.informer.InformerTypeFinderTest.these;
import static org.junit.Assert.assertThat;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import com.dooapp.gaedo.finders.informers.DateFieldInformer;
import com.dooapp.gaedo.informer.InformerInfos;
import com.dooapp.gaedo.informer.InformerMojo;
import com.dooapp.gaedo.informer.InformerTypeFinder;

/**
 * Location of this test is a quick hack to obtain the right package name without relying upon a basic string
 * @author ndx
 *
 */
public class InformerMojoTestForBasicBeans {

	private static final String TEST_PATH = "/src/test/resources/";
	private static File postFile;
	private static File userFile;
	private static File tagFile;
	private static File stateFile;
	private static File themeFile;
	private InformerMojo tested;

	@BeforeClass
	public static void loadInformerMojoFile() {
		String folder = System.getProperty("user.dir") + TEST_PATH + InformerMojoTestForBasicBeans.class.getPackage().getName().replace('.', '/')+"/";
		postFile = new File(folder + "Post.java");
		stateFile = new File(folder + "State.java");
		userFile = new File(folder + "User.java");
		tagFile = new File(folder + "Tag.java");
		// Allows cross-ppackage informer use
		themeFile = new File(folder+"specific/" + "Theme.java");
	}

	@Before
	public void setUp() throws Exception {
		tested = new InformerMojo();
		tested.project = new MavenProject();
		tested.setOutput(new File(System.getProperty("user.dir") + "/target/generated/informers"));
		tested.project.getCompileSourceRoots().add(new File(System.getProperty("user.dir") + TEST_PATH).getCanonicalPath());
		tested.requiredInterfaces = new String[] { Serializable.class.getCanonicalName() };
		tested.requiredAnnotations = new String[] { Named.class.getCanonicalName() };
	}
	
	@Test
	public void stateIsAnEnum() {
		Collection<String> enums = tested.findQualifiedEnums(Arrays.asList(postFile, stateFile, userFile, tagFile, themeFile));
		assertThat(enums, IsCollectionContaining.hasItem("com.dooapp.gaedo.test.beans.State"));
	}


	/**
	 * Test compilation units generation
	 * @throws MojoExecutionException
	 */
	@Test
	public void allElementsOfSourcesAreProcessed() throws MojoExecutionException {
		tested.requiredAnnotations = new String[] { };
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(postFile, userFile, stateFile, tagFile, themeFile));
		assertThat(sources.size(), Is.is(4));
	}

	/**
	 * Test for http://gaedo.origo.ethz.ch/issues/44
	 * @throws MojoExecutionException
	 */
	@Test
	public void postFileListWorksGoodForBug44() throws MojoExecutionException {
		tested.requiredAnnotations = new String[] { };
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(postFile));
		assertThat(sources.size(), IsNot.not(0));
		CompilationUnit u = sources.get(0);
		InformerInfos infos = tested.extractInformerOf(u, false, these());
		assertThat(infos.className, Is.is("Post"));
		assertThat(infos.classPackage, Is.is(getClass().getPackage().getName()));
		assertThat(infos.properties.size(), IsNot.not(0));
		assertThat(InformerTypeFinder.getInformerTypeFor(
						com.dooapp.gaedo.informer.Utils.createResolvedInformers(), 
						these(), 
						these("java.lang", "java.util"), 
						infos.getInfosFor("tags")), 
					Is.is("CollectionFieldInformer<Tag>"));
	}

	/**
	 * Test for date, which does not work well
	 * @throws MojoExecutionException
	 */
	@Test
	public void postFileListWorksGoodForDateFieldInformer() throws MojoExecutionException {
		tested.requiredAnnotations = new String[] { };
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(postFile));
		assertThat(sources.size(), IsNot.not(0));
		CompilationUnit u = sources.get(0);
		InformerInfos infos = tested.extractInformerOf(u, false, these());
		assertThat(infos.className, Is.is("Post"));
		assertThat(infos.classPackage, Is.is(getClass().getPackage().getName()));
		assertThat(infos.properties.size(), IsNot.not(0));
		assertThat(InformerTypeFinder.getInformerTypeFor(
						com.dooapp.gaedo.informer.Utils.createResolvedInformers(), 
						these(), 
						these("java.lang", "java.util"), 
						infos.getInfosFor("publicationDate")), 
					Is.is(DateFieldInformer.class.getSimpleName()));
	}

	/**
	 * Test for http://gaedo.origo.ethz.ch/issues/45
	 * @throws MojoExecutionException
	 */
	@Test
	public void userFileListWorksGoodForBug45() throws MojoExecutionException {
		tested.requiredAnnotations = new String[] { };
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(userFile));
		assertThat(sources.size(), IsNot.not(0));
		CompilationUnit u = sources.get(0);
		InformerInfos infos = tested.extractInformerOf(u, false, these());
		assertThat(infos.className, Is.is("User"));
		String packageName = getClass().getPackage().getName();
		assertThat(infos.classPackage, Is.is(packageName));
		assertThat(infos.properties.size(), IsNot.not(0));
		Collection<String> imports = these("java.lang", "java.util");
		for(ImportDeclaration i : infos.imports) {
			imports.add(i.getName().toString());
		}
		assertThat(InformerTypeFinder.getInformerTypeFor(
						com.dooapp.gaedo.informer.Utils.createResolvedInformers(), 
						these(), 
						imports, 
						infos.getInfosFor("theme")), 
					Is.is("ThemeInformer"));
		assertThat(imports, IsCollectionContaining.hasItem("com.dooapp.gaedo.test.beans.specific.Theme"));
		assertThat(imports, IsCollectionContaining.hasItem("com.dooapp.gaedo.test.beans.specific.ThemeInformer"));
	}

	/**
	 * Test for the properties part of http://gaedo.origo.ethz.ch/issues/46
	 * @throws MojoExecutionException
	 */
	@Test
	public void postFileListWorksGoodForBug46() throws MojoExecutionException {
		tested.requiredAnnotations = new String[] { };
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(postFile));
		assertThat(sources.size(), IsNot.not(0));
		CompilationUnit u = sources.get(0);
		InformerInfos infos = tested.extractInformerOf(u, false, these("note", "com.dooapp.gaedo.test.beans.Post#publicationDate"));
		assertThat(infos.getInfosFor("note"), 
					IsNull.nullValue());
		assertThat(infos.getInfosFor("publicationDate"), 
						IsNull.nullValue());
	}

	/**
	 * Test for class specified as 
	 * @throws MojoExecutionException
	 */
	@Test
	public void themeFileClassSupportIsOK() throws MojoExecutionException {
		tested.requiredAnnotations = new String[] { };
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(themeFile));
		assertThat(sources.size(), IsNot.not(0));
		CompilationUnit u = sources.get(0);
		InformerInfos infos = tested.extractInformerOf(u, false, these());
		assertThat(infos.className, Is.is("Theme"));
		assertThat(infos.properties.size(), IsNot.not(0));
		Collection<String> imports = these("java.lang", "java.util");
		for(ImportDeclaration i : infos.imports) {
			imports.add(i.getName().toString());
		}
		assertThat(InformerTypeFinder.getInformerTypeFor(
						com.dooapp.gaedo.informer.Utils.createResolvedInformers(), 
						these(), 
						imports, 
						infos.getInfosFor("allowedUser")), 
					Is.is("ClassFieldInformer<? extends User>"));
	}
}

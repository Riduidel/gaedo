package com.dooapp.gaedo.informer;

import static com.dooapp.gaedo.informer.InformerTypeFinderTest.these;
import static org.junit.Assert.assertThat;
import japa.parser.ast.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

public class InformerMojoBasicTest {

	private static File informerMojoFile;
	private InformerMojo tested;
	
	@BeforeClass
	public static void loadInformerMojoFile() {
		informerMojoFile = new File(System.getProperty("user.dir")+"/src/main/java/"+InformerMojo.class.getCanonicalName().replace('.', '/')+".java");
	}

	@Before
	public void setUp() throws Exception {
		tested = new InformerMojo();
		tested.project = new MavenProject();
		tested.setOutput(new File(System.getProperty("user.dir")+"/target/generated/informers"));
		tested.project.getCompileSourceRoots().add(new File(System.getProperty("user.dir")+"/src/main/java").getCanonicalPath());
		tested.requiredInterfaces = new String[] { Serializable.class.getCanonicalName() };
		tested.requiredAnnotations = new String[] { Named.class.getCanonicalName() };
	}

	@Test
	public void projectHasSourceFiles() throws MojoExecutionException {
		Collection<File> sources = tested.getSourceFiles(tested.project.getCompileSourceRoots());
		assertThat(sources, IsCollectionContaining.hasItems(informerMojoFile));
	}

	@Test
	public void InformerMojoIsSerializable() throws MojoExecutionException {
		Collection<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(informerMojoFile));
		assertThat(sources.size(), IsNot.not(0));
	}

	@Test
	public void InformerMojoAssociatedInformerHasCorrectInfos() throws MojoExecutionException {
		List<CompilationUnit> sources = tested.findMatchingSources(Arrays.asList(informerMojoFile));
		assertThat(sources.size(), IsNot.not(0));
		CompilationUnit u = sources.get(0);
		InformerInfos infos = tested.extractInformerOf(u, false, these());
		assertThat(infos.className, Is.is(InformerMojo.class.getSimpleName()));
		assertThat(infos.classPackage, Is.is(InformerMojo.class.getPackage().getName()));
		assertThat(infos.properties.size(), IsNot.not(0));
	}
}

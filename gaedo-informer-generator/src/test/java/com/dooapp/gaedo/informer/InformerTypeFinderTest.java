package com.dooapp.gaedo.informer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.hamcrest.core.Is;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class InformerTypeFinderTest {

	private static Map<String, Class> resolved;
	
	@Parameters
	public static Collection<Object[]> values() {
		Collection<Object[]> returned = new LinkedList<Object[]>();
		String stringInformer = BasicFieldInformerLocator.getInformersMapping().get(String.class).getSimpleName();
		String mapInformer = BasicFieldInformerLocator.getInformersMapping().get(Map.class).getSimpleName();
		String collectionInformer = BasicFieldInformerLocator.getInformersMapping().get(Collection.class).getSimpleName();
		// now build the cases
		returned.add(test("String", stringInformer, these(), these("java.lang", "java.io")));
		returned.add(test("long", BasicFieldInformerLocator.getInformersMapping().get(Long.TYPE).getSimpleName(), these(), these("java.lang", "java.io")));
		returned.add(test("Long", BasicFieldInformerLocator.getInformersMapping().get(Long.TYPE).getSimpleName(), these(), these("java.lang", "java.io")));
		returned.add(test("Map", mapInformer, these(), these("java.lang", "java.io", "java.util")));
		returned.add(test("Collection", collectionInformer, these(), these("java.lang", "java.io", "java.util")));
		returned.add(test("Map<String, String>", mapInformer+"<String, String>", these(), these("java.lang", "java.io", "java.util")));
		returned.add(test("Collection<String>", collectionInformer+"<String>", these(), these("java.lang", "java.io", "java.util.Collection")));
		returned.add(test("Collection<Post>", collectionInformer+"<Post>", these(), these("java.lang", "java.io", "java.util")));
		returned.add(test("State", BasicFieldInformerLocator.getInformersMapping().get(Enum.class).getSimpleName(), these("com.dooapp.gaedo.test.beans.State"), these("java.lang", "com.dooapp.gaedo.test.beans")));
		return returned;
	}
	
	/**
	 * Even more syntactic sugar (come on, it's just a test !)
	 * @param s
	 * @return
	 */
	public static Collection<String> these(String...s) {
		return new LinkedList<String>(Arrays.asList(s));
	}
	
	/**
	 * Some syntactic sugar
	 * @param values
	 * @return
	 */
	private static Object[] test(Object...values) {
		return values;
	}
	
	private final String typeName;

	private final String expected;
	private final Collection<String> imports;

	private Collection<String> enums;

	public InformerTypeFinderTest(String typeName, String expected, Collection<String> enums, Collection<String> imports) {
		super();
		this.typeName = typeName;
		this.expected = expected;
		this.enums = enums;
		this.imports = imports;
	}
	

	@BeforeClass
	public static void setUp() throws Exception {
		resolved = com.dooapp.gaedo.informer.Utils.createResolvedInformers();
	}

	@Test
	public void testValue() {
		assertThat(InformerTypeFinder.getNamedInformerTypeFor(resolved, enums, imports, typeName), Is.is(expected));
	}
}

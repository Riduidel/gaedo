package com.dooapp.gaedo.prevalence.services;

import java.io.File;
import java.util.List;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.matchers.IsCollectionContaining;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.prevalence.space.ExecutionSpace;
import com.dooapp.gaedo.prevalence.space.basic.SimpleExecutionSpace;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;

public class PrevalenceFinderServiceTest {
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private FinderCrudService<Tag, TagInformer> tagService;
	
	public static final ServiceRepository create() {
		ServiceRepository repository = new SimpleServiceRepository();
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		ProxyBackedInformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);

		
		File prevalent = new File(".", PrevalenceFinderServiceTest.class.getName());
		prevalent.deleteOnExit();
		prevalent.mkdirs();
		ExecutionSpace<String> toUse = new SimpleExecutionSpace<String>(prevalent);
		// Now add some services
		repository.add(new PrevalenceFinderService<Tag, TagInformer>(Tag.class, TagInformer.class, proxyInformerFactory, toUse));

		return repository;
	}
	
	@AfterClass
	public static void deletePrevalentSpace() {
		File prevalent = new File(".", PrevalenceFinderServiceTest.class.getName());
		for(File f: prevalent.listFiles()) {
			f.delete();
		}
		prevalent.delete();
	}

	@Before
	public void loadService() {
		ServiceRepository repository = PrevalenceFinderServiceTest.create();
		tagService = repository.get(Tag.class);
	}
	
	/**
	 * As it is a prevalent space (and as a consequence a memorized one), all its content has to be removed
	 */
	@After
	public void cleanService() {
		for(Tag t : tagService.findAll()) {
			tagService.delete(t);
		}
	}
	
	/**
	 * Generate a tag name unique to method
	 * @param initial
	 * @return
	 */
	private String name(String initial) {
		StackTraceElement parent = new Exception().getStackTrace()[1];
		return parent.getMethodName()+" "+initial;
	}

	@Test
	public void testCreate() {
		Tag input = new Tag(name(A));
		Tag returned = tagService.create(input);
		Assert.assertEquals(returned.getText(), input.getText());
	}

	@Test
	public void testFindByName() {
		final String nameA = name(A);
		Tag a = new Tag(nameA);
		tagService.create(a);
		Tag b = tagService.create(new Tag(name(B)));
		Tag c = tagService.create(new Tag(name(C)));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.get("text").equalsTo(nameA);
					}
				}).getAll();
		Assert.assertThat(values, IsInstanceOf.instanceOf(List.class));
		List<Tag> valuesAscollection = (List<Tag>) values;
		Assert.assertThat(valuesAscollection.size(), Is.is(1));
		Assert.assertThat(valuesAscollection.get(0), Is.is(a));
	}

	@Test
	public void testFindUsingEqualsTo() {
		final Tag a = new Tag(name(A));
		tagService.create(a);
		Tag b = tagService.create(new Tag(name(B)));
		Tag c = tagService.create(new Tag(name(C)));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.equalsTo(a);
					}
				}).getAll();
		Assert.assertThat(values, IsInstanceOf.instanceOf(List.class));
		List<Tag> valuesAscollection = (List<Tag>) values;
		Assert.assertThat(valuesAscollection.size(), Is.is(1));
		Assert.assertThat(valuesAscollection.get(0), Is.is(a));
	}

	@Test
	public void testFindByTwoPossibleNames() {
		final String nameA = name(A);
		Tag a = new Tag(nameA);
		tagService.create(a);
		final String nameB = name(B);
		Tag b = tagService.create(new Tag(nameB));
		Tag c = tagService.create(new Tag(name(C)));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						FieldInformer field = object.get("text");
						return Expressions.or(field.equalsTo(nameA), field
								.equalsTo(nameB));
					}
				}).getAll();
		Assert.assertThat(values, IsInstanceOf.instanceOf(List.class));
		List<Tag> valuesAscollection = (List<Tag>) values;
		Assert.assertThat(valuesAscollection.size(), Is.is(2));
		Assert.assertThat(valuesAscollection, IsCollectionContaining.hasItem(a));
		Assert.assertThat(valuesAscollection, IsCollectionContaining.hasItem(b));
	}

	/**
	 * This is the deadly test that check that synthetic getter calls work
	 * correctly
	 */
	@Test
	public void testFindByTwoPossibleNamesWithSyntheticAccessor() {
		final String nameA = name(A);
		Tag a = new Tag(nameA);
		tagService.create(a);
		final String nameB = name(B);
		Tag b = tagService.create(new Tag(nameB));
		Tag c = tagService.create(new Tag(name(C)));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						StringFieldInformer field = object.getText();
						return Expressions.or(field.equalsTo(nameA), field
								.equalsTo(nameB));
					}
				}).getAll();
		Assert.assertThat(values, IsInstanceOf.instanceOf(List.class));
		List<Tag> valuesAscollection = (List<Tag>) values;
		Assert.assertThat(valuesAscollection.size(), Is.is(2));
		Assert.assertThat(valuesAscollection, IsCollectionContaining.hasItem(a));
		Assert.assertThat(valuesAscollection, IsCollectionContaining.hasItem(b));
	}
}

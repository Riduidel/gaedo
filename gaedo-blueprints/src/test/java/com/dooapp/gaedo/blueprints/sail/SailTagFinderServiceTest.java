package com.dooapp.gaedo.blueprints.sail;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.GraphProvider;
import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.blueprints.providers.Neo4j;
import com.dooapp.gaedo.blueprints.providers.Tinker;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import static com.dooapp.gaedo.blueprints.TestUtils.*;

@RunWith(Parameterized.class)
public class SailTagFinderServiceTest extends AbstractSailGraphTest {
	private FinderCrudService<Tag, TagInformer> tagService;
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}
	
	public SailTagFinderServiceTest(GraphProvider graph) {
		super(graph);
	}

	@Before
	public void loadService() throws Exception {
		super.loadService();
		// Now add some services
		repository.add(createServiceFor(Tag.class, TagInformer.class));
		repository.add(createServiceFor(Post.class, PostInformer.class));
		tagService = repository.get(Tag.class);
	}
	
	@After
	public void unload() {
		graph.shutdown();
		File f = new File(GraphProvider.GRAPH_DIR);
		f.delete();
	}

	@Test
	public void testCreate() {
		Tag input = new Tag(A).withId(1l);
		Tag returned = tagService.create(input);
		Assert.assertEquals(returned.getText(), input.getText());
		tagService.delete(input);
	}

	@Test
	public void testFindByName() {
		Tag a = new Tag(A).withId(2l);
		tagService.create(a);
		Tag b = tagService.create(new Tag(B).withId(3l));
		Tag c = tagService.create(new Tag(C).withId(4l));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.get("text").equalsTo(A);
					}
				}).getAll();
		Iterator<Tag> valuesIterator = values.iterator();
		assertThat(valuesIterator.hasNext(), Is.is(true));
		assertThat(valuesIterator.next(), Is.is(a));
		// may have next, as previous tests may fail
		tagService.delete(a);
		tagService.delete(b);
		tagService.delete(c);
	}

	@Test
	public void testFindUsingEqualsTo() {
		final Tag a = new Tag(A).withId(5l);
		tagService.create(a);
		Tag b = tagService.create(new Tag(B).withId(6l));
		Tag c = tagService.create(new Tag(C).withId(7l));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.getText().equalsTo(A);
					}
				}).getAll();
		Iterator<Tag> valuesIterator = values.iterator();
		assertThat(valuesIterator.hasNext(), Is.is(true));
		assertThat(valuesIterator.next(), Is.is(a));
		// may have next, as previous tests may fail
		tagService.delete(a);
		tagService.delete(b);
		tagService.delete(c);
	}

	@Test
	public void testFindByTwoPossibleNames() {
		Tag a = new Tag(A).withId(8l);
		tagService.create(a);
		Tag b = tagService.create(new Tag(B).withId(9l));
		Tag c = tagService.create(new Tag(C).withId(10l));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						FieldInformer field = object.get("text");
						return Expressions.or(field.equalsTo(A), field
								.equalsTo(B));
					}
				}).getAll();
		Collection<Tag> matching = new LinkedList<Tag>();
		for(Tag t : values) {
			matching.add(t);
		}
		assertThat(matching, IsCollectionContaining.hasItems(a, b));
		// TODO test
		tagService.delete(a);
		tagService.delete(b);
		tagService.delete(c);
	}

	/**
	 * This is the deadly test that check that synthetic getter calls work
	 * correctly
	 */
	@Test
	public void testFindByTwoPossibleNamesWithSyntheticAccessor() {
		Tag a = new Tag(A).withId(11l);
		tagService.create(a);
		Tag b = tagService.create(new Tag(B).withId(12l));
		Tag c = tagService.create(new Tag(C).withId(13l));
		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {

					public QueryExpression createMatchingExpression(
							TagInformer object) {
						StringFieldInformer field = object.getText();
						return Expressions.or(field.equalsTo(A), field
								.equalsTo(B));
					}
				}).getAll();
		Collection<Tag> matching = new LinkedList<Tag>();
		for(Tag t : values) {
			matching.add(t);
		}
		assertThat(matching, IsCollectionContaining.hasItems(a, b));
		tagService.delete(a);
		tagService.delete(b);
		tagService.delete(c);
	}

	/**
	 * Ensures string type work
	 */
	@Test
	public void ensureStringTypeWorks() {
		Tag a = new Tag(A).withId(12l);
		a.rendering = String.class;
		tagService.create(a);
		Tag b = tagService.find().matching(
						new QueryBuilder<TagInformer>() {

							public QueryExpression createMatchingExpression(
									TagInformer object) {
								return object.getId().equalsTo(12l);
							}
						}).getFirst();
		assertThat(b.rendering, Is.is(Class.class));
	}

	/**
	 * Ensures string type work
	 */
	@Test
	public void ensureBooleanClassTypeWorks() {
		Tag a = new Tag(A).withId(13l);
		a.rendering = Boolean.class;
		tagService.create(a);
		Tag b = tagService.find().matching(
						new QueryBuilder<TagInformer>() {

							public QueryExpression createMatchingExpression(
									TagInformer object) {
								return object.getId().equalsTo(12l);
							}
						}).getFirst();
		assertThat(b.rendering, Is.is(Class.class));
	}

	/**
	 * Ensures string type work
	 */
	@Test
	public void ensureBooleanTypeTypeWorks() {
		Tag a = new Tag(A).withId(14l);
		a.rendering = Boolean.TYPE;
		tagService.create(a);
		Tag b = tagService.find().matching(
						new QueryBuilder<TagInformer>() {

							public QueryExpression createMatchingExpression(
									TagInformer object) {
								return object.getId().equalsTo(12l);
							}
						}).getFirst();
		assertThat(b.rendering, Is.is(Class.class));
	}
}

package com.dooapp.gaedo.blueprints;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;

import static com.dooapp.gaedo.blueprints.TestUtils.A;
import static com.dooapp.gaedo.blueprints.TestUtils.B;
import static com.dooapp.gaedo.blueprints.TestUtils.C;
import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class GraphTagFinderServiceTest extends AbstractGraphTest {
	
	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}
	
	public GraphTagFinderServiceTest(AbstractGraphEnvironment<?> graph) {
		super(graph);
	}

	@Test
	public void testCreate() {
		Tag input = new Tag(A).withId(1l);
		Tag returned = getTagService().create(input);
		Assert.assertEquals(returned.getText(), input.getText());
		getTagService().delete(input);
	}

	@Test
	public void testFindByName() {
		Tag a = new Tag(A).withId(2l);
		getTagService().create(a);
		Tag b = getTagService().create(new Tag(B).withId(3l));
		Tag c = getTagService().create(new Tag(C).withId(4l));
		Iterable<Tag> values = getTagService().find().matching(
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
		getTagService().delete(a);
		getTagService().delete(b);
		getTagService().delete(c);
	}

	@Test
	public void testFindUsingEqualsTo() {
		final Tag a = new Tag(A).withId(5l);
		getTagService().create(a);
		Tag b = getTagService().create(new Tag(B).withId(6l));
		Tag c = getTagService().create(new Tag(C).withId(7l));
		Iterable<Tag> values = getTagService().find().matching(
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
		getTagService().delete(a);
		getTagService().delete(b);
		getTagService().delete(c);
	}

	@Test
	public void testFindByTwoPossibleNames() {
		Tag a = new Tag(A).withId(8l);
		getTagService().create(a);
		Tag b = getTagService().create(new Tag(B).withId(9l));
		Tag c = getTagService().create(new Tag(C).withId(10l));
		Iterable<Tag> values = getTagService().find().matching(
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
		getTagService().delete(a);
		getTagService().delete(b);
		getTagService().delete(c);
	}

	/**
	 * This is the deadly test that check that synthetic getter calls work
	 * correctly
	 */
	@Test
	public void testFindByTwoPossibleNamesWithSyntheticAccessor() {
		Tag a = new Tag(A).withId(11l);
		getTagService().create(a);
		Tag b = getTagService().create(new Tag(B).withId(12l));
		Tag c = getTagService().create(new Tag(C).withId(13l));
		Iterable<Tag> values = getTagService().find().matching(
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
		getTagService().delete(a);
		getTagService().delete(b);
		getTagService().delete(c);
	}

	/**
	 * Ensures string type work
	 */
	@Test
	public void ensureStringTypeWorks() {
		Tag a = new Tag(A).withId(12l);
		a.rendering = String.class;
		getTagService().create(a);
		Tag b = getTagService().find().matching(
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
		a = getTagService().create(a);
		Tag b = getTagService().find().matching(
						new QueryBuilder<TagInformer>() {

							public QueryExpression createMatchingExpression(
									TagInformer object) {
								return object.getId().equalsTo(13l);
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
		a = getTagService().create(a);
		Tag b = getTagService().find().matching(
						new QueryBuilder<TagInformer>() {

							public QueryExpression createMatchingExpression(
									TagInformer object) {
								return object.getId().equalsTo(14l);
							}
						}).getFirst();
		assertThat(b.rendering, Is.is(Class.class));
	}
}

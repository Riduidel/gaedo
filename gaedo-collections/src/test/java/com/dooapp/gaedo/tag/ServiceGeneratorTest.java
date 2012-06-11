package com.dooapp.gaedo.tag;

import java.util.Collection;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.exceptions.finder.dynamic.MethodBindingException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingFieldException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingModeException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingQueryExpressionException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingSortingExpressionException;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.dynamic.DynamicFinder;
import com.dooapp.gaedo.finders.dynamic.ServiceGenerator;
import com.dooapp.gaedo.finders.dynamic.ServiceGeneratorImpl;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;

public class ServiceGeneratorTest {
	public interface CorrectService extends DynamicFinder<Tag, TagInformer> {
		/**
		 * This one works !
		 * @param name
		 * @return
		 */
		public List<Tag> findAllByTextStartsWith(String name);
	}

	public interface MissingFieldService extends DynamicFinder<Tag, TagInformer> {

		/**
		 * This method declaration won't work, since the nam attribute does not exists in the Tag class
		 * @param name
		 * @return
		 */
		public Collection<Tag> findAllByNameStartsWith(String name);
	}

	public interface IncorrectExpressionService extends DynamicFinder<Tag, TagInformer> {

		/**
		 * This one does not work since method name is startsWith, and not startingWith
		 * @param name
		 * @return
		 */
		public Collection<Tag> findAllByTextStartingWith(String name);
	}

	public interface MissingModeService extends DynamicFinder<Tag, TagInformer> {
		
		/**
		 * This declaration won't work, as the prefix is not good
		 * @param name
		 * @return
		 */
		public Tag badStartWithNameStartsWith(String name);
	}

	public interface BadRangeParameterOffsetService extends DynamicFinder<Tag, TagInformer> {
		
		/**
		 * This declaration won't work, as the parameters associated with service range don't have the right class (second one is a double)
		 * @param name
		 * @return
		 */
		public Tag findRangeByTextStartsWith(int start, double end, String name);
	}

	public interface CorrectSortingService extends DynamicFinder<Tag, TagInformer> {
		public Tag findOneWithTextStartsWithSortByTextAscending(String name);
	}

	public interface CorrectUseOfInheritedId extends DynamicFinder<Tag, TagInformer> {
		public Tag findOneWithIdEqualsTo(long i);
	}

	public interface BadSortingService extends DynamicFinder<Tag, TagInformer> {
		/**
		 * This one should fail since sorting texts can be Ascending or Descending, but not Asc
		 * @param name
		 * @return
		 */
		public Tag findOneWithTextStartsWithSortByTextAsc(String name);
	}

	public interface CountService extends DynamicFinder<Tag, TagInformer> {
		public int countByTextStartsWith(String name);
	}

	public interface TagsWithId extends DynamicFinder<Tag, TagInformer>, IdBasedService<Tag> {
	}

	private ServiceGenerator generator = new ServiceGeneratorImpl(new FieldBackedPropertyProvider());
	private FinderCrudService backEnd;

	@Before
	public void generateService() {
		backEnd = (FinderCrudService) TestEnvironmentProvider
				.create().get(Tag.class);
		backEnd.create(new Tag("a"));
		backEnd.create(new Tag("aa"));
		backEnd.create(new Tag("aA"));
		backEnd.create(new Tag("b"));
	}

	@Test
	public void testCorrectServiceGeneration() {
		CorrectService service = (CorrectService) generator.generate(
				CorrectService.class, backEnd);
		List<Tag> results = service.findAllByTextStartsWith("a");
		Assert.assertThat(results.size(), Is.is(3));
	}

	/**
	 * Since field does not exists, method generation won't work here
	 */
	@Test(expected = UnableToBuildDueToMissingFieldException.class)
	public void testBadServiceGenerationForIncorrectField() {
		MissingFieldService service = (MissingFieldService) generator.generate(
				MissingFieldService.class, backEnd);
	}

	/**
	 * Since expression does not exists, method generation won't work here
	 */
	@Test(expected=UnableToBuildDueToMissingQueryExpressionException.class)
	public void testBadServiceGenerationForIncorrectExpressionName() {
		IncorrectExpressionService service = (IncorrectExpressionService) generator.generate(
				IncorrectExpressionService.class, backEnd);
	}

	/**
	 * Since expression does not exists, method generation won't work here
	 */
	@Test(expected=UnableToBuildDueToMissingModeException.class)
	public void testBadServiceGenerationForIncorrectMode() {
		MissingModeService service = (MissingModeService) generator.generate(
				MissingModeService.class, backEnd);
	}

	/**
	 * Since expression does not exists, method generation won't work here
	 */
	@Test
	public void testCorrectCount() {
		CountService service = (CountService) generator.generate(
				CountService.class, backEnd);
		Assert.assertThat(service.countByTextStartsWith("a"), Is.is(3));
	}

	/**
	 * Observe how ranges are not defined using the same type
	 */
	@Test(expected=MethodBindingException.class)
	public void testBadRangeParameterTypeCount() {
		BadRangeParameterOffsetService service = (BadRangeParameterOffsetService) generator.generate(
				BadRangeParameterOffsetService.class, backEnd);
	}

	@Test
	public void testCorrectUseOfInheritedField() {
		CorrectUseOfInheritedId service = (CorrectUseOfInheritedId) generator.generate(
						CorrectUseOfInheritedId.class, backEnd);
	}

	/**
	 * Since expression does not exists, method generation won't work here
	 */
	@Test
	public void testCorrectSortingService() {
		CorrectSortingService service = (CorrectSortingService) generator.generate(
				CorrectSortingService.class, backEnd);
	}

	/**
	 * Since expression does not exists, method generation won't work here
	 */
	@Test(expected=UnableToBuildDueToMissingSortingExpressionException.class)
	public void testBadSortingService() {
		BadSortingService service = (BadSortingService) generator.generate(
				BadSortingService.class, backEnd);
	}

	/**
	 * Test that a service providing IdBasedService methods works OK
	 */
	@Test
	public void testTagsWithId() {
		TagsWithId service = (TagsWithId) generator.generate(
						TagsWithId.class, backEnd);
	}
}

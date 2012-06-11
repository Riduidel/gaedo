package com.dooapp.gaedo.rest.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.SortingExpression.Direction;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionVisitor;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;


public class SortingTranscripterTest {
	private SortingTranscripter tested;
	private ServiceRepository repository;

	@Before
	public void prepare() {
		tested = new SortingTranscripter();
		repository = TestEnvironmentProvider.create();
	}

	@Test
	public void testBuildForUser() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("sort[login]", "Ascending");
		testData.put("sort[password]", "Descending");
		
		final UserInformer informer = (UserInformer) repository.get(User.class).getInformer();
		SortingBuilder<Informer<User>> sorting = tested.buildSorting(repository.get(User.class), testData);
		SortingExpression expr = sorting.createSortingExpression(informer);
		expr.accept(new SortingExpressionVisitor() {
			
			@Override
			public void visit(Entry<FieldInformer, Direction> entry) {
				if(entry.getKey().equals(informer.getPassword()))
					Assert.assertThat(entry.getValue(), Is.is(SortingExpression.Direction.Descending));
				else if(entry.getKey().equals(informer.getLogin()))
					Assert.assertThat(entry.getValue(), Is.is(SortingExpression.Direction.Ascending));
				else
					Assert.fail("this field does not exists");
					
			}
			
			@Override
			public void startVisit(SortingExpression sortingExpression) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void endVisit(SortingExpression sortingExpression) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Test
	public void testBuildForUserWithNonExistingField() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("sort[that field does not exists]", "Ascending");
		testData.put("sort[password]", "Descending");
		
		final UserInformer informer = (UserInformer) repository.get(User.class).getInformer();
		SortingBuilder<Informer<User>> sorting = tested.buildSorting(repository.get(User.class), testData);
		SortingExpression expr = sorting.createSortingExpression(informer);
		expr.accept(new SortingExpressionVisitor() {
			
			@Override
			public void visit(Entry<FieldInformer, Direction> entry) {
				if(entry.getKey().equals(informer.getPassword()))
					Assert.assertThat(entry.getValue(), Is.is(SortingExpression.Direction.Descending));
				else
					Assert.fail("this field does not exists");
			}
			
			@Override
			public void startVisit(SortingExpression sortingExpression) {
				
			}
			
			@Override
			public void endVisit(SortingExpression sortingExpression) {
				
			}
		});
	}
}

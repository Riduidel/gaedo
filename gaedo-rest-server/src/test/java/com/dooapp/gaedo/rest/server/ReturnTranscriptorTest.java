package com.dooapp.gaedo.rest.server;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.rest.server.ReturnTranscriptor.UnsupportedModeException;
import com.dooapp.gaedo.test.beans.User;


public class ReturnTranscriptorTest {
	private ReturnTranscriptor tested;
	private ServiceRepository repository;

	@Before
	public void prepare() {
		tested = new ReturnTranscriptor();
		repository = TestEnvironmentProvider.create();
	}

	@Test
	public void testCountUserWithLogin() {
		QueryStatement<User, User, Informer<User>> toExecute = repository.get(User.class).find().matching(new QueryBuilder<Informer<User>>() {

			@Override
			public QueryExpression createMatchingExpression(Informer<User> informer) {
				return informer.get("login").equalsTo("first");
			}
		});
		Map<String, Object> returnData = new LinkedHashMap<String, Object>();
		returnData.put("return[mode]", "countBy");

		Object result = tested.buildReturn(toExecute, returnData);
		Assert.assertThat(result, IsInstanceOf.instanceOf(Integer.class));
		Assert.assertThat((Integer) result, Is.is(1));
	}

	@Test
	public void testFindFirstUserWithLogin() {
		QueryStatement<User, User, Informer<User>> toExecute = repository.get(User.class).find().matching(new QueryBuilder<Informer<User>>() {

			@Override
			public QueryExpression createMatchingExpression(Informer<User> informer) {
				return informer.get("login").equalsTo("first");
			}
		});
		Map<String, Object> returnData = new LinkedHashMap<String, Object>();
		returnData.put("return[mode]", "findOneWith");

		Object result = tested.buildReturn(toExecute, returnData);
		Assert.assertThat(result, IsInstanceOf.instanceOf(User.class));
		User returned = (User) result;
		Assert.assertThat(returned.getLogin(), Is.is("first"));
		Assert.assertThat(returned.id, Is.is(1l));
	}

	@Test(expected=UnsupportedModeException.class)
	public void testFindUserWithBadReturnData() {
		QueryStatement<User, User, Informer<User>> toExecute = repository.get(User.class).find().matching(new QueryBuilder<Informer<User>>() {

			@Override
			public QueryExpression createMatchingExpression(Informer<User> informer) {
				return informer.get("login").equalsTo("first");
			}
		});
		Map<String, Object> returnData = new LinkedHashMap<String, Object>();
		returnData.put("return[mode]", "bleuah");

		Object result = tested.buildReturn(toExecute, returnData);
	}

	@Test
	public void testFindUserRangeFailingDueToMissingParameters() {
		QueryStatement<User, User, Informer<User>> toExecute = repository.get(User.class).find().matching(new QueryBuilder<Informer<User>>() {

			@Override
			public QueryExpression createMatchingExpression(Informer<User> informer) {
				return informer.get("login").equalsTo("first");
			}
		});
		Map<String, Object> returnData = new LinkedHashMap<String, Object>();
		returnData.put("return[mode]", "findRangeBy");
		// Notice the use of default values

		Object result = tested.buildReturn(toExecute, returnData);
		Assert.assertThat(result, IsInstanceOf.instanceOf(Iterable.class));
		Iterable<User> returned = (Iterable<User>) result;
		for(User u : returned) {
			Assert.assertThat(u.getLogin(), Is.is("first"));
			Assert.assertThat(u.id, Is.is(1l));
		}
		// There is only one result
	}

	@Test
	public void testFindAllUsers() {
		QueryStatement<User, User, Informer<User>> toExecute = repository.get(User.class).find().matching(new QueryBuilder<Informer<User>>() {

			@Override
			public QueryExpression createMatchingExpression(Informer<User> informer) {
				return informer.get("login").equalsTo("first");
			}
		});
		Map<String, Object> returnData = new LinkedHashMap<String, Object>();
		returnData.put("return[mode]", "findAllBy");

		Object result = tested.buildReturn(toExecute, returnData);
	}
}

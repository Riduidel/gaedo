package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.UserInformer;

public class FindUserByLogin implements QueryBuilder<UserInformer> {
	@Override
	public QueryExpression createMatchingExpression(UserInformer informer) {
		return informer.getLogin().equalsTo(TestUtils.USER_LOGIN);
	}
}
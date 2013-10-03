package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;

public class FindPostByAuthorLogin implements QueryBuilder<PostInformer> {

	private String userLogin;

	public FindPostByAuthorLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	@Override
	public QueryExpression createMatchingExpression(PostInformer informer) {
		return informer.getAuthor().getLogin().equalsTo(userLogin);
	}

}

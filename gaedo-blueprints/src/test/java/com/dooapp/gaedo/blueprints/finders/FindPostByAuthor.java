package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.User;

public class FindPostByAuthor implements QueryBuilder<PostInformer> {
	private final User author;
	public FindPostByAuthor(User author) {
		super();
		this.author = author;
	}
	@Override
	public QueryExpression createMatchingExpression(PostInformer informer) {
		return informer.getAuthor().equalsTo(author);
	}
}
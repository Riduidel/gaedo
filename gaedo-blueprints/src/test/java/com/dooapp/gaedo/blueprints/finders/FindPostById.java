package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;

public class FindPostById implements QueryBuilder<PostInformer> {

	private long postId;

	public FindPostById(long postId) {
		this.postId = postId;
	}

	@Override
	public QueryExpression createMatchingExpression(PostInformer informer) {
		return informer.getId().equalsTo(postId);
	}

}

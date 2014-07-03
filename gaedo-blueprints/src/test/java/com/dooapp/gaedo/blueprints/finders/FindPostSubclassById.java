package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;

public class FindPostSubclassById implements QueryBuilder<PostSubClassInformer> {

	private long postId;

	public FindPostSubclassById(long postId) {
		this.postId = postId;
	}

	@Override
	public QueryExpression createMatchingExpression(PostSubClassInformer informer) {
		return informer.getId().equalsTo(postId);
	}

}

package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;

public class FindFirstPostByNote implements QueryBuilder<PostInformer> {
	@Override
	public QueryExpression createMatchingExpression(PostInformer informer) {
		return informer.getNote().equalsTo(1);
	}
}
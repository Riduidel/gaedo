package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;

public class FindPostByText implements QueryBuilder<PostInformer> {
	private final String text;

	public FindPostByText(String text) {
		this.text = text;
	}

	@Override
	public QueryExpression createMatchingExpression(PostInformer informer) {
		return informer.getText().equalsTo(text);
	}
}
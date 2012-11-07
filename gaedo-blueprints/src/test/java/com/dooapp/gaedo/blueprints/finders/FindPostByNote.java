package com.dooapp.gaedo.blueprints.finders;

import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.PostInformer;

public class FindPostByNote implements QueryBuilder<PostInformer> {
	private final Number note;
	
	public FindPostByNote(Number note) {
		this.note = note;
	}
	
	@Override
	public QueryExpression createMatchingExpression(PostInformer informer) {
		return informer.getNote().equalsTo(note);
	}
}
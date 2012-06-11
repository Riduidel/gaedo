package com.dooapp.gaedo.finders.sort;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.SortingExpression;

public class SortingExpressionImpl implements SortingExpression {
	/**
	 * Sorting directives are here stored as a map, to ensure no field is used to sort in both directions
	 */
	private Map<FieldInformer, Direction> directives = new LinkedHashMap<FieldInformer, Direction>();

	@Override
	public SortingExpressionImpl add(FieldInformer informer, Direction direction) {
		directives.put(informer, direction);
		return this;
	}

	@Override
	public Iterator<Entry<FieldInformer, Direction>> iterator() {
		return directives.entrySet().iterator();
	}

	@Override
	public void accept(SortingExpressionVisitor visitor) {
		visitor.startVisit(this);
		for(Map.Entry<FieldInformer, Direction> entry : directives.entrySet()) {
			visitor.visit(entry);
		}
		visitor.endVisit(this);
	}

}

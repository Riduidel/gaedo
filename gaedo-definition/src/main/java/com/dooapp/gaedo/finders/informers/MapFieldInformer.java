package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.MapContainingKeyExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Field informer for a map considered as such.
 * 
 * TODO like for the collection, I should find a way to continue navigation in map. However, i don't have the beginning of an idea of the way to do it.
 * @author ndx
 *
 * @param <ContainedKeyInformer>
 * @param <ContainedValueInformer>
 */
public class MapFieldInformer<KeyType, ValueType>
		extends ObjectFieldInformer {

	public MapFieldInformer(Property source) {
		super(source);
	}

	public QueryExpression containingKey(KeyType contained) {
		return new MapContainingKeyExpression(source, getFieldPath(), contained);
	}

	public QueryExpression containingValue(ValueType contained) {
		return new MapContainingValueExpression(source, getFieldPath(), contained);
	}
	
	@Override
	protected MapFieldInformer clone() {
		return new MapFieldInformer<KeyType, ValueType>(source);
	}
}

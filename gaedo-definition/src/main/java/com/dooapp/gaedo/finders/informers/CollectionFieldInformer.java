package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.CollectionContaingExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Field informer for collections considered only as such. 
 * TODO Theorically, generics should be used for model navigation. But for now, i don't have the slighest idea on the way to do it !
 * @author ndx
 *
 * @param ValueType type of value that the associated collection (the one found at source path) contains
 * @param InformerType the type of informer used to perform query upon values.
 */
public class CollectionFieldInformer<ValueType> extends ObjectFieldInformer {

	public CollectionFieldInformer(Property source) {
		super(source);
	}

	/**
	 * Expects the collection to contain a given object
	 * @param contained expected contained object
	 * @return a {@link CollectionContaingExpression}
	 */
	public QueryExpression containing(Object contained) {
		return new CollectionContaingExpression(source, getFieldPath(),contained);
	}
	
	@Override
	protected CollectionFieldInformer<ValueType> clone(){
		return new CollectionFieldInformer<ValueType>(source);
	}
}

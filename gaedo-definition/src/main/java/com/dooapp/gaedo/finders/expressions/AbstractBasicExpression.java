package com.dooapp.gaedo.finders.expressions;

import java.util.Collections;

import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.properties.Property;

public abstract class AbstractBasicExpression implements QueryExpression {
	/**
	 * Field on which the equality is checked. This field may be null. In such a case, the comparison must be done on object iself.
	 */
	private final Property field;
	
	
	/**
	 * Complete path allowing access to the property from the Informer that declares it. This property allows indirection following, what a simple {@link #field}
	 * doesn't allow in any fashion.
	 */
	private final Iterable<Property> fieldPath;

	public AbstractBasicExpression(Property fieldName, Iterable<Property> fieldPath) {
		super();
		this.field = fieldName;
		if(fieldPath==null) {
			this.fieldPath = Collections.emptyList();
		} else {
			this.fieldPath = fieldPath;
		}
	}

	/**
	 * Get field associated to expression.
	 * This field may be null. In such a case, comparison is done on object itesfl.
	 * @return a may be null field
	 */
	public Property getField() {
		return field;
	}

	/**
	 * Uses {@link Expressions#toString(QueryExpression))} to output a string
	 * view
	 */
	@Override
	public String toString() {
		return Expressions.toString(this);
	}

	/**
	 * Complete navigation from source Informer to the field used for this expression. This iterable can't be null, but it can be empty.
	 * @return the fieldPath
	 * @category getter
	 * @category fieldPath
	 */
	public Iterable<Property> getFieldPath() {
		return fieldPath;
	}
}

package com.dooapp.gaedo.finders.expressions;

import com.dooapp.gaedo.properties.Property;

/**
 * Base class for containing expression
 * @author ndx
 *
 */
public abstract class AbstractBasicContainingExpression extends AbstractBasicExpression {

	/**
	 * Contained obejct expectation
	 */
	private final Object contained;


	public AbstractBasicContainingExpression(Property fieldName,Iterable<Property> path, 
			Object contained) {
		super(fieldName, path);
		this.contained = contained;
	}


	public Object getContained() {
		return contained;
	}
}

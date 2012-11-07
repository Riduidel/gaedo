package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.ContainsStringExpression;
import com.dooapp.gaedo.finders.expressions.EndsWithExpression;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.properties.Property;

/**
 * Informer for fields containing String values
 * @author ndx
 *
 */
public class StringFieldInformer extends ObjectFieldInformer implements
		FieldInformer {

	public StringFieldInformer(Property source) {
		super(source);
	}
	
	/**
	 * Create an expression checking this field contains the given String
	 * @param contained string that field should contain
	 * @return a {@link ContainsStringExpression}
	 */
	public QueryExpression contains(String contained) {
		return new ContainsStringExpression(source, getFieldPath(), contained);
	}
	
	/**
	 * Creates an expression checking this field starts with the given string
	 * @param start text that should be at start of this field
	 * @return a {@link StartsWithExpression}
	 */
	public QueryExpression startsWith(String start) {
		return new StartsWithExpression(source, getFieldPath(), start);
	}
	
	/**
	 * Creates an expression checking this field ends with the given string
	 * @param end text that should be at end of this field
	 * @return a {@link EndsWithExpression}
	 */
	public QueryExpression endsWith(String end) {
		return new EndsWithExpression(source, getFieldPath(), end);
	}
	
	@Override
	protected StringFieldInformer clone() {
		return new StringFieldInformer(source);
	}
}

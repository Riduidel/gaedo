package com.dooapp.gaedo.finders;

import java.lang.reflect.Field;

import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Get informations about fields, and allow use of equality checks
 *
 * @author Nicolas
 *
 * @param <InformedType> informed type, can be used for later type routing (in other words better methods definintions)
 */
public interface FieldInformer<InformedType> {
	/**
	 * Check if field is equals to value
	 *
	 * @param value
	 *            checked value
	 * @return a query expression checking. A new one will be created each time.
	 */
	public QueryExpression equalsTo(Object value);

	/**
	 * Get the field this informer talks about
	 * @return
	 */
	public Property getField();

	/**
	 * Get collection of properties allowing access to that field from the root informer used to perform query
	 * @return an iterable object allowing browsing of property path. Notice this iterable MUST be an infinitely reusable one (in other words, it's a good policy to return a new instance each time).
	 */
	public Iterable<Property> getFieldPath();
}

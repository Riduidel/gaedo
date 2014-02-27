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
	 * Refining in accessed type. It is indeed possible to have some subtypes accessed through parent services.
	 * In such a case, it is useful to be able to restrict on a given type. This expression allows that kind of search.
	 * Notice operation is equivalent to "this instanceof type" and, as a consequence, is not a strict equals, but
	 * rather a "classes contains type".
	 * @param type type we want returned objects to be instances of. it is of course a subtype of this informed type.
	 */
	public QueryExpression instanceOf(Class<? extends InformedType> type);


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

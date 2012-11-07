package com.dooapp.gaedo.finders;

import java.util.Collection;

import com.dooapp.gaedo.properties.Property;

/**
 * Internal purpose interface allowing manipulation of objects in a "transparent" fashion.
 * Any call to methods of this interface from external objects may have undesried behaviour (like the end of the world, as an example).
 * @author ndx
 *
 */
public interface FieldInformerAPI extends FieldInformer {

	/**
	 * Create a usable version of this object. In fact, source object should never be directly return, as effectively used one
	 * will have a fieldPath set, which make it non-reusable.
	 * @param propertyPath TODO
	 * @return usually an {@link Object#clone()} call is enough
	 */
	FieldInformer with(Collection<Property> propertyPath);

}

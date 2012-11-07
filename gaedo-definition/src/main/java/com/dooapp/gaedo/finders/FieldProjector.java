package com.dooapp.gaedo.finders;

import com.dooapp.gaedo.properties.Property;

/**
 * A field projector has the ability to be seen as a FieldInformer
 * @author ndx
 *
 */
public interface FieldProjector <Informed extends Object> {
	/**
	 * Transform this object into a field informer
	 * @param field field containing an object of this type
	 * @return a projected informer
	 */
	Informer<Informed> asField(Property field);
}

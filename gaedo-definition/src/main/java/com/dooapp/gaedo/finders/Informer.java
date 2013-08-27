package com.dooapp.gaedo.finders;

import java.util.Collection;

import com.dooapp.gaedo.properties.Property;

/**
 * Informer is the root interface for objects providing informations upon a
 * given class.<br>
 * To a certain extend, it can be seen as an example of introspection
 * specialization.<br/>
 * In order to allow usage of the {@link FieldInformer#equalsTo(Object)}, this interface is a subinterface of the {@link FieldInformer} interface.
 * @author Nicolas
 *
 */
public interface Informer<Informed extends Object> extends FieldInformerAPI<Informed>, FieldProjector<Informed> {
	/**
	 * get field informer for one of the fields of current object
	 * @param string
	 * @return a field informer for the given property name
	 */
	FieldInformer get(String string);
	/**
	 * get field informer for one of the fields of current object with a given property path.
	 * @param string
	 * @param propertyPath collection of properties allowing access to this one
	 * @return a field informer for the given property name
	 */
	FieldInformer get(String string, Collection<Property> propertyPath);

	/**
	 * Get list of all field informers in this informer
	 * @return
	 */
	Collection<FieldInformer> getAllFieldInformers();

	/**
	 * Get list of all fields in this informer
	 * @return
	 */
	Collection<Property> getAllFields();
}

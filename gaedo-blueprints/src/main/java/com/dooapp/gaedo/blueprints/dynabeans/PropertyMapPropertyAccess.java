package com.dooapp.gaedo.blueprints.dynabeans;

import java.util.List;

import com.dooapp.gaedo.properties.Property;

/**
 * Gaedo internal interface implemented for clean access from graph properties to bean properties.
 * Public should NOT use this interface.
 * @author ndx
 *
 */
public interface PropertyMapPropertyAccess {

	/**
	 * Get value from the given property
	 * @param graphProperty
	 * @return value associated to property
	 */
	List<Object> getFrom(Property graphProperty);

	/**
	 * Set value from the given property
	 * @param graphProperty
	 * @param value
	 */
	void setFrom(Property graphProperty, Object value);

	/**
	 * Get available property uris
	 * @return a collection of loaded property uris
	 */
	Iterable<Property> propertyUris();

	void dont_use_this_interface_which_is_gaedo_specific_customers_should_use_property_bag_interface();
}

package com.dooapp.gaedo.blueprints.dynabeans;

import com.dooapp.gaedo.properties.Property;

/**
 * Gaedo internal interface implemented for clean access from graph properties to bean properties.
 * Public should NOT use this interface.
 * @author ndx
 *
 */
public interface PropertyMapPropertyAccess {

	Object getFrom(Property graphProperty);

	void setFrom(Property graphProperty, Object value);

}

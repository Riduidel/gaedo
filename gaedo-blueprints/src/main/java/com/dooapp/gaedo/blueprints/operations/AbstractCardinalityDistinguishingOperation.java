package com.dooapp.gaedo.blueprints.operations;

import java.util.Collection;
import java.util.Map;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.properties.Property;

/**
 * A base class distinguishing various cardinality on operations.
 * @author ndx
 *
 */
abstract class AbstractCardinalityDistinguishingOperation implements Operation {

	/**
	 * As it has been observed that operations are differenciated for Object, Collections and Maps, this methods detects property type then, based upon that type, call the right abstract one
	 * @param p
	 * @param cascade
	 * @see com.dooapp.gaedo.blueprints.operations.Operation#operateOn(com.dooapp.gaedo.properties.Property, javax.persistence.CascadeType)
	 */
	@Override
	public void operateOn(Property p, CascadeType cascade) {
		Class<?> rawPropertyType = p.getType();
		if (Collection.class.isAssignableFrom(rawPropertyType)) {
			operateOnCollection(p, cascade);
			// each value should be written as an independant value
		} else if (Map.class.isAssignableFrom(rawPropertyType)) {
			operateOnMap(p, cascade);
		} else {
			operateOnSingle(p, cascade);
		}
	}

	protected abstract void operateOnSingle(Property p, CascadeType cascade);

	protected abstract void operateOnMap(Property p, CascadeType cascade);

	protected abstract void operateOnCollection(Property p, CascadeType cascade);

}

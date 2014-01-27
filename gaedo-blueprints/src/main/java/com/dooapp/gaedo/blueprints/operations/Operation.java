package com.dooapp.gaedo.blueprints.operations;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.properties.Property;

/**
 * Operation to perform on each property
 * @author ndx
 *
 */
interface Operation {

	void operateOn(Property p, CascadeType cascade);

}
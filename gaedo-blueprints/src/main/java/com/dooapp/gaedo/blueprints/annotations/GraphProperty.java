package com.dooapp.gaedo.blueprints.annotations;

/**
 * A graph property is a gaedo-specific annotation allowing one to define
 * @author ndx
 *
 */
public @interface GraphProperty {
	/**
	 * Property name. When using a property from a namespace, the namespace prefix is to be included in that name. Notice this namespace prefix will be expanded only in SailGraphBackedCrudService.
	 * @return graph property name.
	 */
	String name();

}

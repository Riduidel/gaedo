package com.dooapp.gaedo.blueprints.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;

/**
 * A graph property is a gaedo-specific annotation allowing one to define
 * @author ndx
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphProperty {
	/**
	 * Property name. When using a property from a namespace, the namespace prefix is to be included in that name. Notice this namespace prefix will be expanded only in SailGraphBackedCrudService.
	 * @return graph property name.
	 */
	String name() default "";

	/**
	 * Mapping strategy used to map java value to graph edge value. may (or not) be used
	 * @return
	 */
	PropertyMappingStrategy mapping() default PropertyMappingStrategy.prefixed;
}

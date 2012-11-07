package com.dooapp.gaedo.blueprints.annotations;

/**
 * Collection of namespaces usable in this entity
 * @author ndx
 *
 */
public @interface Namespaces {
	/**
	 * Collection of namespaces used in an entity. When empty, default namespaces are used
	 * @return
	 */
	public Namespace[] value();
}

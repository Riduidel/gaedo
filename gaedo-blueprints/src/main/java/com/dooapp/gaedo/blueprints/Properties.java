package com.dooapp.gaedo.blueprints;



/**
 * List of properties used by nodes
 * @author ndx
 *
 */
public enum Properties {
	/** contains the effective value */
	value,
	/**
	 * Contain one of {@link Kind} enum values
	 */
	kind, 
	/**
	 * Contains the semantic version of property type
	 */
	type,
	/**
	 * Contains the edge label (not appliable to vertices)
	 */
	label,
	/**
	 * Indicates at which index in the Collection an Edge (or rather, the Node to which
	 * the Edge links) should appear.
	 */
	collection_index;
}
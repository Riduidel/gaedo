package com.dooapp.gaedo.blueprints;


/**
 * List of properties used by nodes
 * @author ndx
 *
 */
public enum Properties {
	/** Contains the class name of data */
	type,
	/** Contains the kind of data contaied by this node. value is an element of the {@link Kind} enum */
	kind,
	/** contains the effective value */
	value,
	/** Contains object id, as Neo4J doesn't seem to care about the id we write */
	vertexId;
}
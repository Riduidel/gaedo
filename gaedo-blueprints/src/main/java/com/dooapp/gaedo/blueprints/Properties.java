package com.dooapp.gaedo.blueprints;


/**
 * List of properties used by nodes
 * @author ndx
 *
 */
public enum Properties {
	/** Contains the class name of data */
	type,
	/** contains the effective value */
	value,
	/** Contains object id, as Neo4J doesn't seem to care about the id we write */
	vertexId;
}
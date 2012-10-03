package com.dooapp.gaedo.blueprints;

/**
 * 
 * @author ndx
 *
 */
public enum Kind {
	/**
	 * Indicates object is a complex one. it can be either managed by a service or a tuple.
	 */
	uri,
	/**
	 * Indicates object is a literal value, in other words a sin gle value, independant from any other one
	 */
	literal,
	/**
	 * Stands for blank node and is of use only by tuple transformers
	 */
	bnode;
	
}
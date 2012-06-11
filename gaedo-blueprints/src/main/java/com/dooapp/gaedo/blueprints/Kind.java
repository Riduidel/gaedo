package com.dooapp.gaedo.blueprints;

/**
 * 
 * @author ndx
 *
 */
public enum Kind {
	/**
	 * Indicates object is managed by a service
	 */
	managed,
	/**
	 * Indicates object is a literal value, in other words a sin gle value, independant from any other one
	 */
	literal,
	/**
	 * Indicates object is a vertex tuple : a vertex with no other meaning than to link other vertices. To a certain extend, 
	 * managed nodes are an extension of tuple ones (as they also mainly link multiple vertices together). However, the concept of tuple node don't imply the presence of a 
	 * service managing tuple nodes, as they're quite standard ones.
	 */
	tuple;
	
}
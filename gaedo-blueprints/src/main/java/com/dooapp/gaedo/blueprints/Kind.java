package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.blueprints.transformers.TypeUtils;

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

	private static final String BNODE_PREFIX = "_:";


	/**
	 * Get URI for the given vertex id and type. URI form will depend upon the current kind
	 * @param vertexId
	 * @param valueClass
	 * @return
	 */
	public String getURIFor(String vertexId, Class<? extends Object> valueClass) {
		switch(this) {
		case uri:
			return getUriURIFor(vertexId, valueClass);
		case literal:
			return getLiteralURIFor(vertexId, valueClass);
		case bnode:
			return getBNodeURIFor(vertexId, valueClass);
		default:
			throw new UnsupportedOperationException("Kind "+name()+" is not supported");
		}
	}
	

	private String getBNodeURIFor(String vertexId, Class<? extends Object> valueClass) {
		// I feel so dirty to write that !
		return BNODE_PREFIX+vertexId;
	}


	private String getUriURIFor(String vertexId, Class<? extends Object> valueClass) {
		return valueClass.getName()+":"+vertexId;
	}


	private String getLiteralURIFor(String vertexId, Class<? extends Object> valueClass) {
		// i'm sorry, but it seems to be Sail standard URI for Literals
		return String.format("\"%s\"^^<%s>", vertexId, TypeUtils.getType(valueClass));
	}


	public String extractValueOf(String id) {
		switch(this) {
		case uri:
			return id.substring(id.indexOf(':')+1);
		case literal:
			return id;
		case bnode:
			return id.substring(BNODE_PREFIX.length()+1);
		default:
			throw new UnsupportedOperationException("Kind "+name()+" is not supported");
		}
	}
}
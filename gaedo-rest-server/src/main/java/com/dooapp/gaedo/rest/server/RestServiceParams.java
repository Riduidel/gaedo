package com.dooapp.gaedo.rest.server;

import java.util.Map;

public enum RestServiceParams {
	OBJECT("object"),
	FILTER("filter"),
	SORT("sort"),
	RETURN("return");
	
	private final String prefix;

	private RestServiceParams(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	/**
	 * Get parameters from input request associated to the prefix for this parameter definition
	 * @param requestAttributes
	 * @return
	 */
	public Map<String, Object> getParams(Map<String, Object> requestAttributes) {
		return Utils.getParamsStartingWith(getPrefix(), requestAttributes);
	}
}

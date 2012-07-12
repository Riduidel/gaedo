package com.dooapp.gaedo.blueprints.transformers;

/**
 * Base interface for all kind of transformer, it allows more generic code (like the {@link Transformers} class)
 */
public interface Transformer {
	
	/**
	 * Check if that transformer can handle the given value
	 * @param effectiveType
	 * @return
	 */
	public boolean canHandle(String effectiveType);
}

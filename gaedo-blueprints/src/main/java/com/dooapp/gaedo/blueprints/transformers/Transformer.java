package com.dooapp.gaedo.blueprints.transformers;

/**
 * Base interface for all kind of transformer, it allows more generic code (like the {@link Transformers} class)
 */
public interface Transformer {

	/**
	 * Check if that transformer can handle the given value
	 * @param classLoader classloader used to potentially load effective type
	 * @param effectiveType effective type we want info about
	 * @return true if this transformer can handle the given effective type
	 */
	public boolean canHandle(ClassLoader classLoader, String effectiveType);
}

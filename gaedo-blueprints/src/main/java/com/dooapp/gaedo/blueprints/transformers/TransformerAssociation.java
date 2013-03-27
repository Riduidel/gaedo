package com.dooapp.gaedo.blueprints.transformers;


public interface TransformerAssociation<TransformerKind extends Transformer> {

	Class<?> getDataClass();

	TransformerKind getTransformer();

	/**
	 * Check if that transformer association can handle the hiven value
	 * @param classLoader classloader used to load class
	 * @param effectiveType
	 * @return
	 */
	boolean canHandle(ClassLoader classLoader, String effectiveType);

}

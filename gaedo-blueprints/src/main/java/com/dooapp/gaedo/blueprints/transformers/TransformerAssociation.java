package com.dooapp.gaedo.blueprints.transformers;

public interface TransformerAssociation<TransformerKind extends Transformer> {

	Class<?> getDataClass();

	TransformerKind getTransformer();

}

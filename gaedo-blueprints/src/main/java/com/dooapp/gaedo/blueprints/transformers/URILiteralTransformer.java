package com.dooapp.gaedo.blueprints.transformers;

import java.net.URI;

public class URILiteralTransformer extends AbstractSimpleLiteralTransformer<URI> implements LiteralTransformer<URI> {
	public URILiteralTransformer() {
		super(URI.class);
	}

	@Override
	protected Object getVertexValue(URI value) {
		return value==null ? "null" : value.toString();
	}

}

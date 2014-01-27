package com.dooapp.gaedo.blueprints.transformers;

import java.net.URI;

public class URILiteralTransformer extends AbstractSimpleLiteralTransformer<URI> implements LiteralTransformer<URI> {
	public URILiteralTransformer() {
		super(URI.class);
	}

	@Override
	public String valueToString(URI value) {
		return value==null ? "null" : value.toString();
	}

}

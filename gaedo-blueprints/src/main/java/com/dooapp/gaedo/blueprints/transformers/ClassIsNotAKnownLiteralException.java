package com.dooapp.gaedo.blueprints.transformers;


import java.lang.reflect.Type;
import java.util.Arrays;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class ClassIsNotAKnownLiteralException extends BluePrintsCrudServiceException {

	public ClassIsNotAKnownLiteralException(TransformerAssociation<?>[] values, Class dataClass) {
		this(values, dataClass.getCanonicalName());
	}

	public ClassIsNotAKnownLiteralException(TransformerAssociation<?>[] values, Type t) {
		this(values, t.toString());
	}

	public ClassIsNotAKnownLiteralException(TransformerAssociation<?>[] values, String effectiveType) {
		super("given class "+effectiveType+" is not associated to any transformer. Known literals types are "+Arrays.asList(values));
	}

}

package com.dooapp.gaedo.blueprints.transformers;


import java.util.Arrays;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;

public class ClassIsNotAKnownLiteralException extends BluePrintsCrudServiceException {

	public ClassIsNotAKnownLiteralException(Class dataClass) {
		super("given class "+dataClass.getCanonicalName()+" is not associated to any transformer. Known literals types are "+Arrays.asList(Literals.values()));
	}

}

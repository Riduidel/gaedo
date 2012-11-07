package com.dooapp.gaedo.blueprints;

public class ImpossibleToGetIdOfUnknownType extends BluePrintsCrudServiceException {

	public ImpossibleToGetIdOfUnknownType(Class<? extends Object> valueClass) {
		super("impossible to build id of "+valueClass.getCanonicalName()+". it's unknown of our services, neither a literal, nor a tuple");
	}

}

package com.dooapp.gaedo.properties;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.utils.Utils;

public final class NoAllClassesOfException extends CrudServiceException {
	public NoAllClassesOfException(String utilsClassName, Exception e) {
		super("most of the code of this class depends upon the existence of the "+utilsClassName+"#allClassesof(Class c) method. if it is deleted/renamed, mayhem has happened",e);
	}
}
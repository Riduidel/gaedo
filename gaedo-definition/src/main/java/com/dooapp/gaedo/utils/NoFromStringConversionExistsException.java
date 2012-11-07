package com.dooapp.gaedo.utils;

import com.dooapp.gaedo.CrudServiceException;

public class NoFromStringConversionExistsException extends CrudServiceException {

	public NoFromStringConversionExistsException(Class<?> type, Exception constructorException,
			Exception valueOfException) {
		super("unable to create an object of class "+type.getName()+" from string." +
				"\n\tInvoking constructor raised "+constructorException.getClass().getName()+" "+constructorException.getMessage()+
				"\n\tInvoking valueOf method raised "+valueOfException.getClass().getName()+" "+valueOfException.getMessage());
	}

}

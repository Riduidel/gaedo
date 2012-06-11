/**
 * 
 */
package com.dooapp.gaedo.exceptions.finder.dynamic;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.exceptions.DynamicFinderException;
import com.dooapp.gaedo.finders.FieldInformer;

public class UnableToBuildQueryExpressionException extends DynamicFinderException {

	public UnableToBuildQueryExpressionException(FieldInformer key,
			Method value, Object[] methodArgs, Exception e) {
		super("unable to build query from "+key.toString()+"\nusing method\n"+value.toGenericString()+"\nusing arguments "+Arrays.toString(methodArgs), e);
	}
	
}
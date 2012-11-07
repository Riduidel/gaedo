package com.dooapp.gaedo.exceptions.finder.dynamic;

import java.lang.reflect.Method;
import java.util.Set;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.exceptions.DynamicFinderException;

/**
 * Exception thrown when a field was found, but none of its expression construction methods has the name given
 * @author ndx
 *
 */
public class UnableToBuildDueToMissingQueryExpressionException extends DynamicFinderException {

	public UnableToBuildDueToMissingQueryExpressionException(String consumableText, String methodString, 
			String key, Set<String> keySet) {
		super("a part \""+consumableText+"\" of the method name you wrote \""+methodString+"\" cannot be bound to any existing query expression.\n" +
				"assumed usable expression for field \""+key+"\" names are "+keySet.toString());
	}
	
}
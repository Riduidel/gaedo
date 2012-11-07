package com.dooapp.gaedo.exceptions.finder.dynamic;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.dooapp.gaedo.exceptions.DynamicFinderException;
import com.dooapp.gaedo.finders.SortingExpression.Direction;

/**
 * Expression thrown when dynamic finder sorting close is incorrectly built.
 * @author ndx
 *
 */
public class UnableToBuildDueToMissingSortingExpressionException extends
		DynamicFinderException {

	public UnableToBuildDueToMissingSortingExpressionException(String consumableText,
			String methodString, String key, Direction[] values) {
		super("a part \""+consumableText+"\" of the method name you wrote \""+methodString+"\" cannot be bound to any existing sorting expression.\n" +
				"assumed usable sorting expressions for field \""+key+"\" names are "+Arrays.toString(values));
	}

}

package com.dooapp.gaedo.exceptions.range;

import com.dooapp.gaedo.exceptions.BadRangeException;
import com.dooapp.gaedo.finders.QueryStatement;

/**
 * Exception thrown when invoking a {@link QueryStatement#get(int, int)} with an incorrect range
 * @author ndx
 *
 */
public class BadRangeDefinitionException extends BadRangeException {

	public BadRangeDefinitionException(int start, int end) {
		super("range you entered appears to be invalid.\nStart = "+start+"\nEnd = "+end);
	}

}

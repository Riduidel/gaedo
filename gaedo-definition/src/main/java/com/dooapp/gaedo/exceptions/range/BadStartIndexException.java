package com.dooapp.gaedo.exceptions.range;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.exceptions.BadRangeException;

/**
 * This exception is thrown when start index of a range is lower than 0
 * @author ndx
 *
 */
public class BadStartIndexException extends BadRangeException {

	public BadStartIndexException(int start) {
		super("start index you defined ("+start+") is lower than 0");
	}

}

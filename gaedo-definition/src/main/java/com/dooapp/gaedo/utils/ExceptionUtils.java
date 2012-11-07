package com.dooapp.gaedo.utils;

import java.util.Collection;

/**
 * Class containing some utilities regarding exceptions
 * @author ndx
 *
 */
public class ExceptionUtils {

	public static String collectMessages(
			Collection<? extends Exception> exceptions) {
		StringBuilder sOut = new StringBuilder();
		for(Exception e: exceptions) {
			sOut.append("\n\n\t").append(e.getMessage());
		}
		return sOut.toString();
	}

}

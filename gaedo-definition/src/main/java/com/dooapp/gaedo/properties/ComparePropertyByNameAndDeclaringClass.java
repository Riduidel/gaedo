package com.dooapp.gaedo.properties;

import java.util.Comparator;

/**
 * Compare properties based upon fields they all declare.
 * This comparator should not support by any way having two properties that are equals.
 * @author ndx
 *
 */
public class ComparePropertyByNameAndDeclaringClass implements Comparator<Property> {

	@Override
	public int compare(Property o1, Property o2) {
		int result = o1.getName().compareTo(o2.getName());
		if(result!=0)
			return result;
		result = o1.getDeclaringClass().getName().compareTo(o2.getDeclaringClass().getName());
		if(result!=0)
			return result;
		result = o1.getGenericType().toString().compareTo(o2.getGenericType().toString());
		return result;
	}

}

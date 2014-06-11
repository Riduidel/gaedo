package com.dooapp.gaedo.properties;

import java.util.Comparator;

public class ComparePropertiesOnGenericName implements Comparator<Property> {
	@Override
	public int compare(Property first, Property second) {
		return first.toGenericString().compareTo(second.toGenericString());
	}
}
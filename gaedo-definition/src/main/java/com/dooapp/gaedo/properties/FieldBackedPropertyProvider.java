package com.dooapp.gaedo.properties;

import java.lang.reflect.Field;



public class FieldBackedPropertyProvider implements PropertyProvider {
	@Override
	public Property[] get(Class<?> containedClass) {
		Field[] source = containedClass.getDeclaredFields();
		Property[] returned = new Property[source.length];
		for(int index=0; index < source.length; index++) {
			returned[index] = new PropertyField(source[index]);
		}
		return returned;
	}

}

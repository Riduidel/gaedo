package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.properties.Property;

/**
 * Informer for classes
 * @author ndx
 *
 * @param <SpecifiedType> not used, but required as classes sometimes appear in generics suits
 */
public class ClassFieldInformer<SpecifiedType> extends ObjectFieldInformer {

	public ClassFieldInformer(Property source) {
		super(source);
	}

	@Override
	protected ClassFieldInformer<SpecifiedType> clone() {
		return new ClassFieldInformer<SpecifiedType>(source);
	}
}

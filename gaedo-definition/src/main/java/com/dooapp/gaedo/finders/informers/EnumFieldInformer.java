package com.dooapp.gaedo.finders.informers;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Informer for enums. Should allow (in a not-so-distant future) comparison with strings)
 * @author ndx
 *
 */
public class EnumFieldInformer extends ObjectFieldInformer implements
		FieldInformer {

	public EnumFieldInformer(Property source) {
		super(source);
	}

	@Override
	protected EnumFieldInformer clone() {
		return new EnumFieldInformer(source);
	}
}

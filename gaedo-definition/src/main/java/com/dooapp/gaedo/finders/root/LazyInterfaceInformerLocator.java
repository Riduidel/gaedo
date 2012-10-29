package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.informers.ObjectFieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Informer locator solving the very special case of fields declared using business interface.
 * Most of the time, this case is used to allow implementation of one field using different implementations, 
 * for which link resolution should be done at runtime.
 * @author ndx
 *
 */
public class LazyInterfaceInformerLocator implements FieldInformerLocator {

	/**
	 * Generate an {@link ObjectFieldInformer} for field defined using any non-java.* interface.
	 * @param field
	 * @return
	 * @see com.dooapp.gaedo.finders.root.FieldInformerLocator#getInformerFor(com.dooapp.gaedo.properties.Property)
	 */
	@Override
	public FieldInformer getInformerFor(Property field) {
		Class<?> fieldType = field.getType();
		if(fieldType.isInterface()) {
			if(!fieldType.getPackage().getName().startsWith("java.")) {
				return new ObjectFieldInformer(field);
			}
		}
		return null;
	}

	/**
	 * Don't dream, there is no fallback method here
	 * @param informedClass
	 * @param fieldName
	 * @return
	 * @see com.dooapp.gaedo.finders.root.FieldInformerLocator#getInformerFor(java.lang.Class, java.lang.String)
	 */
	@Override
	public FieldInformer getInformerFor(Class informedClass, String fieldName) {
		return null;
	}

}

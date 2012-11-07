package com.dooapp.gaedo.finders.root;

import java.util.HashMap;
import java.util.Map;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.properties.Property;

public class InformerFieldLocator implements FieldInformerLocator, InformerFactory {
	private Map<Class<?>, Informer<?>> informers = new HashMap<Class<?>, Informer<?>>();
	private InformerFactory real;

	@Override
	public FieldInformer getInformerFor(Property field) {
		if(informers.containsKey(field.getType())) {
			return informers.get(field.getType()).asField(field);
		}
		return null;
	}

	public InformerFactory masquerade(InformerFactory real) {
		this.real = real;
		return this;
	}

	@Override
	public <InformerType extends Informer<ContainedType>, ContainedType> InformerType get(Class<InformerType> informerClass, Class<ContainedType> containedType) {
		InformerType returned = real.get(informerClass, containedType);
		if(!informers.containsKey(containedType)) {
			informers.put(containedType, returned);
		}
		return returned;
	}

	/**
	 * @return the real
	 * @category getter
	 * @category real
	 */
	public InformerFactory getReal() {
		return real;
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

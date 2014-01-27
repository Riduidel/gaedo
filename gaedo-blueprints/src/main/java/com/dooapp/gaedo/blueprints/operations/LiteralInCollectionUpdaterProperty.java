package com.dooapp.gaedo.blueprints.operations;

import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.properties.Property;

public class LiteralInCollectionUpdaterProperty extends AbstractPropertyAdapter implements Property {
	/**
	 * Get used property name
	 * @param p
	 * @param key
	 * @return
	 */
	public static String getName(Property p, Object key) {
		LiteralTransformer transformer = Literals.get(key.getClass());
		return p.getName()+":"+transformer.toString(key);
	}


	private Object value;

	public LiteralInCollectionUpdaterProperty(Property p, Object key, Object value) {
		setDeclaringClass(p.getDeclaringClass());
		setType(value.getClass());
		setGenericType(value.getClass());
		copyAnnotationsFrom(p);
		String name = getName(p, key);
		setName(name);
		this.value = value;
	}

	@Override
	public String toGenericString() {
		return value.toString();
	}

	@Override
	public Object get(Object bean) {
		return value;
	}

	@Override
	public void set(Object bean, Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + LiteralInCollectionUpdaterProperty.class.getName() + "#set has not yet been implemented AT ALL");
	}

	@Override
	public Object fromString(String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + LiteralInCollectionUpdaterProperty.class.getName()
						+ "#fromString has not yet been implemented AT ALL");
	}

}

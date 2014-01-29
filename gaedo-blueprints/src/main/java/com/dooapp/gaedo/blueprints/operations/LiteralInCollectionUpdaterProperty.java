package com.dooapp.gaedo.blueprints.operations;

import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.properties.Property;

/**
 * Delegate property allowing creatin of a "sub" property on vertex.
 * To be more clear, when storing a collection of literals in a vertex, each literal need to have its own property (for loading)
 * and inverse property (for querying). As a consequence, this forged property allow those kinds of operations.
 * @author ndx
 *
 */
public class LiteralInCollectionUpdaterProperty extends AbstractPropertyAdapter implements Property {
	/**
	 * Get used property name
	 * @param p initial property
	 * @param key key of value in property (which is expected to contain a collection)
	 * @return full property name
	 */
	public static String getName(Property p, Object key) {
		LiteralTransformer transformer = Literals.get(key.getClass());
		return getName(p.getName(), transformer.toString(key));
	}

	/**
	 * Get used property name from parent property one and key for property value in parent property.
	 * @param propertyName parent property name
	 * @param propertyKey key for property value in parent. It MUST be obtained through a literal transformer.
	 * @return a property name
	 */
	public static String getName(String propertyName, String propertyKey) {
		return propertyName+":"+propertyKey;
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

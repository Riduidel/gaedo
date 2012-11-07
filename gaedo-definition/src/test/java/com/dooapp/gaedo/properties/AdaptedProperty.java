package com.dooapp.gaedo.properties;

/**
 * Class allowing us to test a few things
 * 
 * @author ndx
 * 
 */
class AdaptedProperty extends AbstractPropertyAdapter {

	@Override
	public String toGenericString() {
		throw new UnsupportedOperationException("method " + Property.class.getName() + "#toGenericString has not yet been implemented AT ALL");
	}

	@Override
	public Object get(Object bean) {
		throw new UnsupportedOperationException("method " + Property.class.getName() + "#get has not yet been implemented AT ALL");
	}

	@Override
	public void set(Object bean, Object value) {
		throw new UnsupportedOperationException("method " + Property.class.getName() + "#set has not yet been implemented AT ALL");
	}

	@Override
	public Object fromString(String value) {
		throw new UnsupportedOperationException("method " + Property.class.getName() + "#fromString has not yet been implemented AT ALL");
	}
}
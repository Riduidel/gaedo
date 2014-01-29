package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

public class TypeProperty extends AbstractPropertyAdapter implements Property {
	/**
	 * Constant instance used to generate some links
	 */
	public static final TypeProperty INSTANCE = new TypeProperty();

	public static final String TYPE = "type";

	public TypeProperty() {
		super();
		setDeclaringClass(Object.class);
		setName(TYPE);
		setType(Class.class);
		setGenericType(Class.class);
		setModifier(Modifier.FINAL, true);
		setModifier(Modifier.PUBLIC, true);
	}

	@Override
	public Object get(Object obj) {
		return obj.getClass();
	}

	@Override
	public String toGenericString() {
		return Object.class.getCanonicalName()+"."+TYPE+" "+getGenericType().toString();
	}

	@Override
	public void set(Object bean, Object value) {
	}

	@Override
	public Object fromString(String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+Property.class.getName()+"#fromString has not yet been implemented AT ALL");
	}

	public TypeProperty withAnnotation(Annotation a) {
		return (TypeProperty) super.withAnnotation(a);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TypeProperty [");
		if (getType() != null) {
			builder.append("getType()=");
			builder.append(getType());
		}
		builder.append("]");
		return builder.toString();
	}
}

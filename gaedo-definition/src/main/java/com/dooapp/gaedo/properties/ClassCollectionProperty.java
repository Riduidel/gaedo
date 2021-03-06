package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import com.dooapp.gaedo.utils.Utils;

public class ClassCollectionProperty extends AbstractPropertyAdapter implements Property {
	public static final String CLASSES = "classes";

	private Class<?> declaring;

	private Collection<Class<?>> values;

	private static Method allClassesOf;

	static {
		try {
			allClassesOf = Utils.class.getDeclaredMethod("allClassesOf", Class.class);
		} catch (Exception e) {
			throw new NoAllClassesOfException(Utils.class.getCanonicalName(), e);
		}
	}

	public ClassCollectionProperty(Class<?> declaring) {
		setDeclaringClass(Object.class);
		setName(CLASSES);
		setGenericType(allClassesOf.getGenericReturnType());
		this.declaring = declaring;
		values = Utils.allClassesOf(declaring);
		setModifier(Modifier.FINAL, true);
		setModifier(Modifier.PUBLIC, true);
	}

	@Override
	public Object get(Object obj) {
		return values;
	}

	@Override
	public String toGenericString() {
		return Object.class.getCanonicalName()+"."+CLASSES+" "+getGenericType().toString();
	}

	@Override
	public void set(Object bean, Object value) {
	}

	@Override
	public Object fromString(String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+Property.class.getName()+"#fromString has not yet been implemented AT ALL");
	}

	public ClassCollectionProperty withAnnotation(Annotation a) {
		return (ClassCollectionProperty) super.withAnnotation(a);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClassCollectionProperty [");
		if (values != null) {
			builder.append("values=");
			builder.append(values);
		}
		builder.append("]");
		return builder.toString();
	}
}

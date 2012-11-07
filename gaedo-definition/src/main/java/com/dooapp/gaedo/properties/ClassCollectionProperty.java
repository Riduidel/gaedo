package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

import com.dooapp.gaedo.utils.Utils;

public class ClassCollectionProperty implements Property {
	public static final String CLASSES = "classes";

	private Class declaring;
	
	private Collection<Class> values;

	private static Method allClassesOf;
	
	static {
		try {
			allClassesOf = Utils.class.getDeclaredMethod("allClassesOf", Class.class);
		} catch (Exception e) {
			throw new NoAllClassesOfException(Utils.class.getCanonicalName(), e);
		}
	}

	public ClassCollectionProperty(Class declaring) {
		this.declaring = declaring;
		values = Utils.allClassesOf(declaring);
	}

	@Override
	public Class<?> getDeclaringClass() {
		return Object.class;
	}
	
	@Override
	public String getName() {
		return CLASSES;
	}
	
	@Override
	public Class<?> getType() {
		return allClassesOf.getReturnType();
	}
	
	@Override
	public Type getGenericType() {
		return allClassesOf.getGenericReturnType();
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
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public Collection<? extends Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasModifier(int modifier) {
		// abstract bit set
		if((modifier & Modifier.ABSTRACT)!=0) {
			return false;
		} else if((modifier & Modifier.FINAL)!=0) {
			return true;
		} else if((modifier & Modifier.INTERFACE)!=0) {
			return false;
		} else if((modifier & Modifier.NATIVE)!=0) {
			return false;
		} else if((modifier & Modifier.PRIVATE)!=0) {
			return false;
		} else if((modifier & Modifier.PROTECTED)!=0) {
			return false;
		} else if((modifier & Modifier.PUBLIC)!=0) {
			return true;
		} else if((modifier & Modifier.STATIC)!=0) {
			return false;
		} else if((modifier & Modifier.STRICT)!=0) {
			return false;
		} else if((modifier & Modifier.SYNCHRONIZED)!=0) {
			return false;
		} else if((modifier & Modifier.TRANSIENT)!=0) {
			return false;
		} else if((modifier & Modifier.VOLATILE)!=0) {
			return false;
		}
		return false;
	}

	@Override
	public Object fromString(String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+Property.class.getName()+"#fromString has not yet been implemented AT ALL");
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((declaring == null) ? 0 : declaring.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassCollectionProperty other = (ClassCollectionProperty) obj;
		if (declaring == null) {
			if (other.declaring != null)
				return false;
		} else if (!declaring.getCanonicalName().equals(other.declaring.getCanonicalName()))
			return false;
		return true;
	}
}

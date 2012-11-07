package com.dooapp.gaedo.extensions.migrable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import com.dooapp.gaedo.properties.Property;

/**
 * Delegating property allowing access to value of a property from a class while pretending to be a property of a different class.
 * @author ndx
 *
 */
public class DelegateProperty implements Property {
	private final String name;
	private final Class<?> declaring;
	private final Property delegate;
	
	public DelegateProperty(String name, Property delegate, Class declaring) {
		super();
		this.declaring = declaring;
		this.name = name;
		this.delegate = delegate;
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#toGenericString()
	 * @category delegate
	 */
	public String toGenericString() {
		return delegate.toGenericString();
	}

	/**
	 * @param bean
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#get(java.lang.Object)
	 * @category delegate
	 */
	public Object get(Object bean) {
		return delegate.get(bean);
	}

	/**
	 * @param bean
	 * @param value
	 * @see com.dooapp.gaedo.properties.Property#set(java.lang.Object, java.lang.Object)
	 * @category delegate
	 */
	public void set(Object bean, Object value) {
		delegate.set(bean, value);
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#getGenericType()
	 * @category delegate
	 */
	public Type getGenericType() {
		return delegate.getGenericType();
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#getType()
	 * @category delegate
	 */
	public Class<?> getType() {
		return delegate.getType();
	}

	/**
	 * @param annotationClass
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#getAnnotation(java.lang.Class)
	 * @category delegate
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return delegate.getAnnotation(annotationClass);
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#getAnnotations()
	 * @category delegate
	 */
	public Collection<? extends Annotation> getAnnotations() {
		return delegate.getAnnotations();
	}

	/**
	 * @param modifier
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#hasModifier(int)
	 * @category delegate
	 */
	public boolean hasModifier(int modifier) {
		return delegate.hasModifier(modifier);
	}

	/**
	 * @param value
	 * @return
	 * @see com.dooapp.gaedo.properties.Property#fromString(java.lang.String)
	 * @category delegate
	 */
	public Object fromString(String value) {
		return delegate.fromString(value);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaring;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DelegateProperty other = (DelegateProperty) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
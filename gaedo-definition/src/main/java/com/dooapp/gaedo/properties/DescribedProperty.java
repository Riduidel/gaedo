package com.dooapp.gaedo.properties;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.dooapp.gaedo.utils.Utils;

/**
 * Property obtained when using beans. Such a property is always associated to a {@link PropertyDescriptor} describing how to access and use it
 * @author ndx
 *
 */
public class DescribedProperty implements Property {

	/**
	 * Property descriptor used to manage property
	 */
	private PropertyDescriptor descriptor;
	
	private Class<?> declaringClass;

	public DescribedProperty(PropertyDescriptor descriptor, Class<?> declaringClass) {
		this.descriptor = descriptor;
		this.declaringClass = declaringClass;
	}

	@Override
	public Object get(Object bean) {
		try {
			return descriptor.getReadMethod().invoke(bean);
		} catch (Exception e) {
			throw new UnableToGetPropertyException(this, e);
		}
	}

	/**
	 * If annotation is present on getter or on getter, returns the first value
	 */
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for(Method m : new Method[] {descriptor.getReadMethod(), descriptor.getWriteMethod()}) {
			if(m.getAnnotation(annotationClass)!=null)
				return m.getAnnotation(annotationClass);
		}
		return null;
	}

	@Override
	public Collection<? extends Annotation> getAnnotations() {
		Collection<Annotation> returned = new HashSet<Annotation>();
		for(Method m : new Method[] {descriptor.getReadMethod(), descriptor.getWriteMethod()}) {
			// beware : for read-only/write-only fields, one o the two methods is null
			if(m!=null) {
				returned.addAll(Arrays.asList(m.getAnnotations()));
			}
		}
		return returned;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public Type getGenericType() {
		return descriptor.getPropertyType();
	}

	@Override
	public String getName() {
		return descriptor.getName();
	}

	@Override
	public Class<?> getType() {
		return descriptor.getPropertyType();
	}

	/**
	 * As a design feature, a bean property has no known modifier (excepted public one)
	 */
	@Override
	public boolean hasModifier(int modifier) {
		return modifier==Modifier.PUBLIC;
	}

	@Override
	public void set(Object bean, Object value) {
		try {
			descriptor.getWriteMethod().invoke(bean, value);
		} catch (Exception e) {
			throw new UnableToSetPropertyException(this, e);
		}
	}

	@Override
	public String toGenericString() {
		return descriptor.toString();
	}

	@Override
	public Object fromString(String value) {
		return Utils.fromString(value, getType());
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
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
		DescribedProperty other = (DescribedProperty) obj;
		if (declaringClass == null) {
			if (other.declaringClass != null)
				return false;
		} else if (!declaringClass.getCanonicalName().equals(other.declaringClass.getCanonicalName()))
			return false;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		return true;
	}
}
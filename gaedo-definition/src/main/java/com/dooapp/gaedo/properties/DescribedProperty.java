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

	private final Class<?> declaringClass;
	/**
	 * Method used to read field value
	 */
	private final Method readMethod;
	/**
	 * Method used to write field value
	 */
	private final Method writeMethod;
	/**
	 * Array of usable methods, used to grab annotations and other infos
	 */
	private final Method[] methods;
	private final String name;
	private final Class<?> type;
	private final String descriptorString;

	/**
	 * Known declaring class of property.
	 * @param descriptor Property descriptor used to provide infos on which methods to call.
	 * Due to the very nature of PropertyDescriptor and its Soft/Weak references to write and read methods, all infos are copied here (we hope PropertyDescriptor is fresh enough
	 * to not have been GCed).
	 * @param declaringClass
	 */
	public DescribedProperty(PropertyDescriptor descriptor, Class<?> declaringClass) {
		this.declaringClass = declaringClass;
		this.readMethod = descriptor.getReadMethod();
		this.writeMethod = descriptor.getWriteMethod();
		this.methods = new Method[] {readMethod, writeMethod};
		type = descriptor.getPropertyType();
		name = descriptor.getName();
		descriptorString = declaringClass.getName()+"#"+name+" ("+type.toString()+")";
	}

	@Override
	public Object get(Object bean) {
		try {
			return readMethod.invoke(bean);
		} catch (Exception e) {
			throw new UnableToGetPropertyException(this, e);
		}
	}

	/**
	 * If annotation is present on getter or on getter, returns the first value
	 */
	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for(Method m : methods) {
			if(m.getAnnotation(annotationClass)!=null)
				return m.getAnnotation(annotationClass);
		}
		return null;
	}

	@Override
	public Collection<? extends Annotation> getAnnotations() {
		Collection<Annotation> returned = new HashSet<Annotation>();
		for(Method m : methods) {
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
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
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
			writeMethod.invoke(bean, value);
		} catch (Exception e) {
			throw new UnableToSetPropertyException(this, e);
		}
	}

	@Override
	public String toGenericString() {
		return descriptorString;
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
		result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
		result = prime * result + ((descriptorString == null) ? 0 : descriptorString.hashCode());
		result = prime * result + ((readMethod == null) ? 0 : readMethod.hashCode());
		result = prime * result + ((writeMethod == null) ? 0 : writeMethod.hashCode());
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
		} else if (!declaringClass.equals(other.declaringClass))
			return false;
		if (descriptorString == null) {
			if (other.descriptorString != null)
				return false;
		} else if (!descriptorString.equals(other.descriptorString))
			return false;
		if (readMethod == null) {
			if (other.readMethod != null)
				return false;
		} else if (!readMethod.equals(other.readMethod))
			return false;
		if (writeMethod == null) {
			if (other.writeMethod != null)
				return false;
		} else if (!writeMethod.equals(other.writeMethod))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toGenericString();
	}
}
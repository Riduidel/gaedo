package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import com.dooapp.gaedo.utils.Utils;

/**
 * Property implemented as a raw field
 * @author ndx
 *
 */
public class PropertyField implements Property {
	/**
	 * Source field
	 */
	protected final Field mapped;

	/**
	 * Notice that, at construction time, contained field is made accessible, allowing the set operation to work
	 * @param mapped
	 */
	public PropertyField(Field mapped) {
		super();
		this.mapped = mapped;
		this.mapped.setAccessible(true);
	}

	@Override
	public Object fromString(String value) {
		return Utils.fromString(value, getType());
	}

	public Object get(Object obj) {
		try {
			return mapped.get(obj);
		} catch (Exception e) {
			throw new UnableToGetPropertyException(this, e);
		}
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return mapped.getAnnotation(annotationClass);
	}

	public Collection<? extends Annotation> getAnnotations() {
		return Arrays.asList(mapped.getAnnotations());
	}

	@Override
	public Class<?> getDeclaringClass() {
		return mapped.getDeclaringClass();
	}

	@Override
	public Type getGenericType() {
		return mapped.getGenericType();
	}
	
	public String getName() {
		return mapped.getName();
	}

	public Class<?> getType() {
		return mapped.getType();
	}

	/**
	 * Method code heavily relies upon implementation found in class {@link java.lang.reflect.Modifier}
	 */
	@Override
	public boolean hasModifier(int modifier) {
		return (mapped.getModifiers() & modifier) != 0;
	}

	public void set(Object obj, Object value) {
		try {
			mapped.set(obj, value);
		} catch (Exception e) {
			throw new UnableToSetPropertyException(this, e);
		}
	}

	public String toGenericString() {
		return mapped.toGenericString();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PropertyField [");
		if (mapped != null) {
			builder.append("mapped=");
			builder.append(mapped);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapped == null) ? 0 : mapped.hashCode());
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
		PropertyField other = (PropertyField) obj;
		if (mapped == null) {
			if (other.mapped != null)
				return false;
		} else if (!mapped.equals(other.mapped))
			return false;
		return true;
	}
}
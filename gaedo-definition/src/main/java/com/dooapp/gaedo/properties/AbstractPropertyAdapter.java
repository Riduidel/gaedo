package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Base class for random properties implementations. This class provides some covnenience methods to easily impelment weird properties
 * @author ndx
 *
 */
public abstract class AbstractPropertyAdapter implements Property {
	private Map<Class<?>, Annotation> annotations = new HashMap<Class<?>, Annotation>();

	/**
	 * Property declaring class
	 */
	private Class<?> declaringClass;

	/**
	 * Property generic type
	 */
	private Type genericType;

	/**
	 * Here, for both laziness and compatibility, modifiers are stored like in Java VM (using bit field and '&' tests).
	 */
	private int modifiers;

	/**
	 * Property name
	 */
	private String name;

	/**
	 * Property class (not to be confused with {@link #genericType}. Notice that this class should be the non-generic version of {@link #genericType}.
	 * As an example, if genericType is List<String>, this field should contain List.
	 */
	private Class<?> type;

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
		AbstractPropertyAdapter other = (AbstractPropertyAdapter) obj;
		if (declaringClass == null) {
			if (other.declaringClass != null)
				return false;
		} else if (!declaringClass.equals(other.declaringClass))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return (T) annotations .get(annotationClass);
	}

	@Override
	public Collection<? extends Annotation> getAnnotations() {
		return annotations.values();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public Type getGenericType() {
		return genericType;
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
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((declaringClass == null) ? 0 : declaringClass.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean hasModifier(int modifier) {
		return (modifiers & modifier) != 0;
	}

	public void setAnnotation(Annotation a) {
		annotations.put(a.annotationType(), a);
	}

	/**
	 * @param declaringClass the declaringClass to set
	 * @category setter
	 * @category declaringClass
	 */
	public void setDeclaringClass(Class<?> declaringClass) {
		this.declaringClass = declaringClass;
	}

	/**
	 * @param declaringClass new value for #declaringClass
	 * @category fluent
	 * @category setter
	 * @category declaringClass
	 * @return this object for chaining calls
	 */
	public AbstractPropertyAdapter withDeclaringClass(Class<?> declaringClass) {
		this.setDeclaringClass(declaringClass);
		return this;
	}

	/**
	 * Set generic type and type accordingly, as we do not want these to be desynchronized.
	 * @param genericType the genericType to set
	 * @category setter
	 * @category genericType
	 */
	public void setGenericType(Type genericType) {
		this.genericType = genericType;
		try {
			setType(genericType);
		} catch(UnusableTypeException e) {
			throw new UnusableTypeException("unable to build a class from input generic type "+genericType.toString(), e);
		}
	}

	/**
	 * Set type from generic type by recursively unfolding all type elements
	 * @param genericType
	 */
	private void setType(Type genericType) {
		if(genericType instanceof Class) {
			setType((Class<?>) genericType);
		} else if(genericType instanceof ParameterizedType) {
			try {
				setType(((ParameterizedType) genericType).getRawType());
			} catch(UnusableTypeException e) {
				throw new UnusableTypeException("unable to use raw type of parameterized type "+genericType.toString(), e);
			}
		} else if(genericType instanceof WildcardType) {
			throw new UnusableTypeException("we can't use as type the wildcard type "+genericType.toString());
		} else if(genericType instanceof TypeVariable) {
			throw new UnusableTypeException("we can't use as type the type variable "+genericType.toString());
		}
	}

	/**
	 * @param genericType new value for #genericType
	 * @category fluent
	 * @category setter
	 * @category genericType
	 * @return this object for chaining calls
	 */
	public AbstractPropertyAdapter withGenericType(Type genericType) {
		this.setGenericType(genericType);
		return this;
	}

	public void setModifier(int modifierFlag, boolean enabled) {
		if(enabled)
			modifiers |= modifierFlag;
		else
			// special construct negating only the bi expressed by the modifierFlag
			modifiers &= ~modifierFlag;
	}

	/**
	 * @param name the name to set
	 * @category setter
	 * @category name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param type the type to set
	 * @category setter
	 * @category type
	 */
	private void setType(Class<?> type) {
		this.type = type;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractPropertyAdapter [");
		if (getDeclaringClass() != null) {
			builder.append("getDeclaringClass()=");
			builder.append(getDeclaringClass());
			builder.append(", ");
		}
		if (getName() != null) {
			builder.append("getName()=");
			builder.append(getName());
			builder.append(", ");
		}
		if (getGenericType() != null) {
			builder.append("getGenericType()=");
			builder.append(getGenericType());
		}
		builder.append("]");
		return builder.toString();
	}

	public AbstractPropertyAdapter withAnnotation(Annotation a) {
		setAnnotation(a);
		return this;
	}

	/**
	 * Copy all annotations of the given property
	 * @param p
	 */
	protected void copyAnnotationsFrom(Property p) {
		for(Annotation a : p.getAnnotations()) {
			setAnnotation(a);
		}
	}
}

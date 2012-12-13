package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

public class TypeProperty extends AbstractPropertyAdapter implements Property {
	/**
	 * Constant instance used to generate some links
	 */
	public static final TypeProperty INSTANCE = new TypeProperty(TypeProperty.class);
	
	public static final String TYPE = "type";
	
	public TypeProperty(Class declaring) {
		super();
		setDeclaringClass(Object.class);
		setName(TYPE);
		setType(declaring);
		setGenericType(declaring);
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

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDeclaringClass() == null) ? 0 : getDeclaringClass().hashCode());
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
		TypeProperty other = (TypeProperty) obj;
		if (getDeclaringClass() == null) {
			if (other.getDeclaringClass() != null)
				return false;
		} else if (!getDeclaringClass().getCanonicalName().equals(other.getDeclaringClass().getCanonicalName()))
			return false;
		return true;
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

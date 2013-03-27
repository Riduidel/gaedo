package com.dooapp.gaedo.blueprints.strategies.graph;

import java.util.Collection;

import com.dooapp.gaedo.blueprints.dynabeans.PropertyMapPropertyAccess;
import com.dooapp.gaedo.blueprints.strategies.BeanIsNotAPropertyBagException;
import com.dooapp.gaedo.blueprints.strategies.UnableToSetTypeException;
import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.utils.Utils;

public class GraphProperty extends AbstractPropertyAdapter {

	private Class<?> containedType;

	@Override
	public Object fromString(String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + GraphProperty.class.getName() + "#fromString has not yet been implemented AT ALL");
	}

	@Override
	public Object get(Object bean) {
		if (bean instanceof PropertyMapPropertyAccess) {
			PropertyMapPropertyAccess bag = (PropertyMapPropertyAccess) bean;
			Object contained = bag.getFrom(this);
			if(contained == null && Collection.class.isAssignableFrom(getType())) {
				contained = Utils.generateCollection(getType(), null);
			}
			return contained;
		} else {
			throw new BeanIsNotAPropertyBagException("bean is a "+bean.getClass().getName());
		}
	}

	@Override
	public void set(Object bean, Object value) {
		if (bean instanceof PropertyMapPropertyAccess) {
			PropertyMapPropertyAccess bag = (PropertyMapPropertyAccess) bean;
			bag.setFrom(this, value);
		} else {
			throw new BeanIsNotAPropertyBagException("bean is a "+bean.getClass().getName());
		}
	}

	public void setContainedTypeName(String containedType) {
		try {
			this.containedType = Class.forName(containedType);
		} catch (ClassNotFoundException e) {
			throw new UnableToSetTypeException("unable to set containedType "+containedType, e);
		}
	}
	
	/**
	 * @param containedTypeName new value for #containedTypeName
	 * @category fluent
	 * @category setter
	 * @category containedTypeName
	 * @return this object for chaining calls
	 */
	public GraphProperty withContainedTypeName(String containedTypeName) {
		this.setContainedTypeName(containedTypeName);
		return this;
	}

	/**
	 * When setting name, additionaly add a GraphProperty annotation to make sure no name is used instead
	 * @param name
	 * @see com.dooapp.gaedo.properties.AbstractPropertyAdapter#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
	}
	
	/**
	 * @param name new value for #name
	 * @category fluent
	 * @category setter
	 * @category name
	 * @return this object for chaining calls
	 */
	public GraphProperty withName(String name) {
		this.setName(name);
		return this;
	}
	
	/**
	 * @param typeName new value for #typeName
	 * @category fluent
	 * @category setter
	 * @category typeName
	 * @return this object for chaining calls
	 */
	public GraphProperty withTypeName(String typeName) {
		this.setTypeName(typeName);
		return this;
	}
	
	@Override
	public GraphProperty withType(Class<?> type) {
		return (GraphProperty) super.withType(type);
	}

	public void setTypeName(String effectiveType) {
		try {
			setType(Class.forName(effectiveType));
		} catch (ClassNotFoundException e) {
			throw new UnableToSetTypeException("unable to set containedType "+containedType, e);
		}
	}
	
	@Override
	public String toGenericString() {
		if(containedType==null)
			return String.format("%s", getType());
		return String.format("%s<%s>", getType(), containedType);
	}

	@Override
	public GraphProperty withDeclaringClass(Class<?> declaringClass) {
		return (GraphProperty) super.withDeclaringClass(declaringClass);
	}
}

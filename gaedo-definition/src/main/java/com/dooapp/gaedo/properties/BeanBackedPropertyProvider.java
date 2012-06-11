package com.dooapp.gaedo.properties;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedList;


public class BeanBackedPropertyProvider implements PropertyProvider {
	/**
	 * get properties from given class <b>only</b>
	 */
	@Override
	public Property[] get(Class<?> containedClass) {
		try {
			BeanInfo info = Introspector.getBeanInfo(containedClass, containedClass.getSuperclass());
			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
			Collection<Property> returned = new LinkedList<Property>();
			for(PropertyDescriptor descriptor : descriptors) {
				returned.add(new DescribedProperty(descriptor, containedClass));
			}
			return returned.toArray(new Property[returned.size()]);
		} catch (IntrospectionException e) {
			throw new NotABeanException(containedClass, e);
		}
	}

}

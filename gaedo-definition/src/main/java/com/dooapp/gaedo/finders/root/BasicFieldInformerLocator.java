package com.dooapp.gaedo.finders.root;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.informers.BooleanFieldInformer;
import com.dooapp.gaedo.finders.informers.ClassFieldInformer;
import com.dooapp.gaedo.finders.informers.CollectionFieldInformer;
import com.dooapp.gaedo.finders.informers.DateFieldInformer;
import com.dooapp.gaedo.finders.informers.DoubleFieldInformer;
import com.dooapp.gaedo.finders.informers.EnumFieldInformer;
import com.dooapp.gaedo.finders.informers.MapFieldInformer;
import com.dooapp.gaedo.finders.informers.ObjectFieldInformer;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Provides field locator behaviour for the basic classes
 * 
 * @author Nicolas
 * 
 */
public class BasicFieldInformerLocator implements FieldInformerLocator {
	/**
	 * Map containing informer mappings. it is not an optimization, but rather a way to provide both sensible defaults and customizable behaviour
	 */
	private Map<Class<?>, Class<? extends FieldInformer>> existingInformerMappings = getInformersMapping();

	/**
	 * get informer for given type 
	 */
	@Override
	public FieldInformer getInformerFor(Property field) {
		Type genericType = field.getGenericType();
		return getInformerFor(field, genericType);
	}

	/**
	 * Get informer for given type, by reflective upon its attribute
	 * @param property source property
	 * @param genericType generic type of property
	 * @return
	 */
	protected FieldInformer getInformerFor(Property property, Type genericType) {
		if (genericType instanceof Class) {
			// Any other case is a class one (look at
			// http://java.sun.com/j2se/1.5.0/docs/api/java/lang/reflect/Type.html
			// for more info)
			Class<?> clazz = (Class<?>) genericType;
			Class<? extends FieldInformer> informerClass = getInformerForClass(clazz);
			// This class must have a constructor using one Property parameter, elsewhere mayhem may come
			Constructor<? extends FieldInformer> used;
			try {
				used = informerClass.getDeclaredConstructor(Property.class);
				return used.newInstance(property);
			} catch (Exception e) {
				throw new UnableToCreateFieldInformerException(clazz, informerClass, property.getClass(), e);
			}
		} else if(genericType instanceof ParameterizedType) {
			// Too lazy to write it all
			ParameterizedType pType = (ParameterizedType) genericType;
			return getInformerFor(property, pType.getRawType());
		}
		return null;
	}
	
	/**
	 * Helper method allowing somee overriding of class localization
	 * @param clazz
	 * @return
	 */
	protected Class<? extends FieldInformer> getInformerForClass(Class<?> clazz) {
		Class<? extends FieldInformer> existingOne = hierarchicalySearchForInformer(clazz);
		if(existingOne==null)
			throw new NoInformerForThisTypeException(clazz);
		else
			return existingOne;
	}
		
	/**
	 * Search in all superclasses of given class for informers
	 * @param clazz source class
	 * @return informer, if found, null elsewhen.
	 */
	private Class<? extends FieldInformer> hierarchicalySearchForInformer(Class<?> clazz) {
		Class<? extends FieldInformer> existingOne = null;
		if(existingInformerMappings.containsKey(clazz)) {
			return existingInformerMappings.get(clazz);
		} else {
			Map<Class<?>, Class<? extends FieldInformer>> foundInformers = new HashMap<Class<?>, Class<? extends FieldInformer>>();
			for(Class<?> interfazz : clazz.getInterfaces()) {
				if(existingInformerMappings.containsKey(clazz))
					foundInformers.put(interfazz, existingInformerMappings.get(clazz));
			}
			if(foundInformers.size()==1) {
				return foundInformers.entrySet().iterator().next().getValue();
			} else if(foundInformers.size()>1) {
				throw new ConflictingInformersForThisTypeException(clazz, foundInformers);
			} else if(Object.class.equals(clazz)) {
				return null;
			} else {
				if(clazz.getSuperclass()!=null) {
					return hierarchicalySearchForInformer(clazz.getSuperclass());
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Create and return a map linking classes to the informers used to represent them. Notice this map is NOT a complete one, but rather provide so-called sensible defaults
	 * @return a map with lots of default mappings
	 */
	public static Map<Class<?>, Class<? extends FieldInformer>> getInformersMapping() {
		Map<Class<?>, Class<? extends FieldInformer>> returned = new HashMap<Class<?>, Class<? extends FieldInformer>>();
		/* 
		 * Default mapping is not set here, in order for exceptions to be thrown fast (when trying to add a field that has no associated mapping).
		 */
//		returned.put(Object.class, ObjectFieldInformer.class);
		// String mapping
		returned.put(String.class, StringFieldInformer.class);
		// Boolean mapping
		returned.put(Boolean.class, BooleanFieldInformer.class);
		returned.put(Boolean.TYPE, BooleanFieldInformer.class);
		// number mappings. Due to dreaded primitive types concerns, There are a *bunch* of them. I feel so dirty for that
		returned.put(Number.class, DoubleFieldInformer.class);
		returned.put(Byte.TYPE, DoubleFieldInformer.class);
		returned.put(Short.TYPE, DoubleFieldInformer.class);
		returned.put(Integer.TYPE, DoubleFieldInformer.class);
		returned.put(Long.TYPE, DoubleFieldInformer.class);
		returned.put(Float.TYPE, DoubleFieldInformer.class);
		returned.put(Double.TYPE, DoubleFieldInformer.class);
		returned.put(Character.TYPE, DoubleFieldInformer.class);
		returned.put(Byte.class, DoubleFieldInformer.class);
		returned.put(Short.class, DoubleFieldInformer.class);
		returned.put(Integer.class, DoubleFieldInformer.class);
		returned.put(Long.class, DoubleFieldInformer.class);
		returned.put(Float.class, DoubleFieldInformer.class);
		returned.put(Double.class, DoubleFieldInformer.class);
		returned.put(Character.class, DoubleFieldInformer.class);
		// Date mapping
		returned.put(Date.class, DateFieldInformer.class);
		// Basic collection mapping
		returned.put(Collection.class, CollectionFieldInformer.class);
		returned.put(List.class, CollectionFieldInformer.class);
		returned.put(Set.class, CollectionFieldInformer.class);
		returned.put(SortedSet.class, CollectionFieldInformer.class);
		// Basic ... map ... mapping
		returned.put(Map.class, MapFieldInformer.class);
		returned.put(SortedMap.class, MapFieldInformer.class);
		returned.put(ConcurrentMap.class, MapFieldInformer.class);
		returned.put(ConcurrentNavigableMap.class, MapFieldInformer.class);
		// property change support is a very special case : we do not want any operation to be provided on it. As a consequence, we map it to object type informer
		returned.put(PropertyChangeSupport.class, ObjectFieldInformer.class);
		// Class has its own information capability
		returned.put(Class.class, ClassFieldInformer.class);
		// So has enum with its custom capabilities
		returned.put(Enum.class, EnumFieldInformer.class);
		// Serializable should be handled (for informer purpose) like Object
		returned.put(Serializable.class, ObjectFieldInformer.class);
		
		
		// Return that collection
		return returned;
	}

	/**
	 * Don't dream, there is no fallback method here
	 * @param informedClass
	 * @param fieldName
	 * @return
	 * @see com.dooapp.gaedo.finders.root.FieldInformerLocator#getInformerFor(java.lang.Class, java.lang.String)
	 */
	@Override
	public FieldInformer getInformerFor(Class informedClass, String fieldName) {
		return null;
	}
}

package com.dooapp.gaedo.finders.root;

import java.util.HashMap;
import java.util.Map;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FieldInformerAPI;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;

/**
 * Global informer factories allow lookup of specific class informers, as long as they're loaded.
 * 
 * @author Nicolas
 * 
 */
public class ReflectionBackedInformerFactory {
	/**
	 * Map linking object classes to reflection backed informers used to gather
	 * field informations for these classes
	 */
	private Map<Class<?>, ReflectionBackedInformer<?>> loaded = new HashMap<Class<?>, ReflectionBackedInformer<?>>();

	/**
	 * Pluggable field informer locator
	 */
	private FieldInformerLocator fieldLocator;
	
	private PropertyProvider propertyProvider;

	public ReflectionBackedInformerFactory(FieldInformerLocator fieldLocator, PropertyProvider provider) {
		super();
		this.fieldLocator = fieldLocator;
		this.propertyProvider = provider;
	}

	/**
	 * Get informer associated to given class
	 * 
	 * @param <T>
	 *            class type
	 * @param clazz
	 *            input class
	 * @return loaded informer for that class
	 */
	public <T> Informer<T> get(Class<T> clazz) {
		if (!loaded.containsKey(clazz)) {
			synchronized (this) {
				if (!loaded.containsKey(clazz)) {
					loaded.put(clazz, new ReflectionBackedInformer<T>(clazz,
							this, propertyProvider));
				}
			}
		}
		return (Informer<T>) loaded.get(clazz);
	}

	/**
	 * This method generates {@link FieldInformer} for each known field type of
	 * object. Furthermore, generated field informer is cloned to make sure it's possible to alter its path
	 * 
	 * @param field
	 *            input field
	 * @return a {@link FieldInformer} if possible, an
	 *         {@link UnsupportedOperationException} is fired elsewhere (this
	 *         way, we will think about handling it later ;-)
	 */
	public FieldInformer getInformerFor(Property field) {
		return fieldLocator.getInformerFor(field);
	}

	/**
	 * @return the fieldLocator
	 * @category getter
	 * @category fieldLocator
	 */
	public FieldInformerLocator getFieldLocator() {
		return fieldLocator;
	}
	
	/**
	 * Delegation method allowing non-existing fields to have a "virtual" existence and be associated to {@link FieldInformer}.
	 * Particularly useful for DB-driven-mapping (as an example querying a generic graph using existent links)
	 * @param informer
	 * @param fieldName
	 * @return
	 */
	public FieldInformer noSuchFieldInHiearchy(ReflectionBackedInformer informer, String fieldName) {
		try  {
			FieldInformer returned = fieldLocator.getInformerFor(informer.getInformedClass(), fieldName);
			if(returned!=null)
				return returned;
		} catch(NoLocatorAllowsFieldException e) {
			// nothing to do : a different place implies a different exception
		}
		throw new NoSuchFieldInHierarchyException(fieldName);
	}
}

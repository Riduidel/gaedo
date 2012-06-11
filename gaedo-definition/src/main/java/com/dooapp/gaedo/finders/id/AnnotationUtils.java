package com.dooapp.gaedo.finders.id;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;

/**
 * Some utility methods regarding id
 * @author ndx
 *
 */
public class AnnotationUtils {

	/**
	 * Locate in given class, with given set of properties, the id one
	 * @param annotation TODO
	 * @param provider property provider, used for superclass properties lookup
	 * @param containedClass source class
	 * @param expectedIdClasses a collection of expected id classes.If that collection is null, no check is performed.
	 */
	public static Property locateOneField(Annotations annotation, PropertyProvider provider, Class<?> containedClass, Collection<Class> expectedIdClasses) {
		return locateOneField(Annotations.ID, provider, containedClass, containedClass, expectedIdClasses);
	}

	/**
	 * Effective implementation method.
	 * @param annotation TODO
	 * @param provider Property provider used to get fields of various classes
	 * @param inputClass the class the user wants an id for
	 * @param currentClass the currently examined class
	 * @param expectedIdClasses possible classes of field containing id
	 * @return property holding the id, or a {@link BadIdAnnotatedClassException} is thrown if none is found
	 */
	private static Property locateOneField(Annotations annotation, PropertyProvider provider, Class<?> inputClass, Class<?> currentClass, Collection<Class> expectedIdClasses) {
		Property[] fields = provider.get(currentClass);
		List<Property> probableId = AnnotationsFinder.findAll(fields,
				Annotations.ID);
		Property idField = null;
		if (probableId.size() == 1) {
			Property potentialIdField = probableId.get(0);
			/*
			 * Someone using Shcrodinger cat in a Java blog post can't be evil
			 * ;-) http://sensualjava.blogspot.com/2007
			 * /11/reflection-and-auto-boxing.html
			 */
			if(expectedIdClasses!=null) {
				boolean found = false;
				Iterator<Class> iterator = expectedIdClasses.iterator();
				while(iterator.hasNext() && !found) {
					found = potentialIdField.getType().isAssignableFrom(iterator.next());
				}
				if(found) {
					idField = potentialIdField;
				}
			}
		}
		// maybe the parent has the solution
		if(idField==null) {
			Class<?> nextClass = currentClass.getSuperclass();
			if(Object.class.equals(nextClass)) {
				throw new BadIdAnnotatedClassException(inputClass, expectedIdClasses);
			} else {
				idField = locateOneField(annotation, provider, inputClass, nextClass, expectedIdClasses);
			}
		}
		return idField;
	}

	/**
	 * Helper method to get rid of stupid compiler warnings
	 * @param provider property provider, used for superclass lookup
	 * @param containedClass examined class
	 * @param idClasses collection of classes usable as id fields
	 * @return
	 */
	public static Property locateIdField(PropertyProvider provider, Class<?> containedClass, Class ...idClasses) {
		return locateOneField(Annotations.ID, provider, containedClass, Arrays.asList(idClasses));
	}

	/**
	 * Get a list of fields having any kind of annotation of this name
	 * @param properties
	 * @param id
	 * @return
	 */
	public static List<Property> locateAllFields(PropertyProvider provider, Class<?> containedClass, Annotations id) {
		return locateAllFields(provider, containedClass, containedClass, id);
	}

	/**
	 * Recursively locate all fields having the given annotation definition
	 * @param provider
	 * @param containedClass
	 * @param currentClass
	 * @param id
	 * @return
	 */
	private static List<Property> locateAllFields(PropertyProvider provider, Class<?> containedClass, Class<?> currentClass, Annotations id) {
		if(!Object.class.equals(currentClass)) {
			Property[] fields = provider.get(currentClass);
			List<Property> returned = AnnotationsFinder.findAll(fields,
					id);
			returned.addAll(locateAllFields(provider, containedClass, currentClass.getSuperclass(), id));
			return returned;
		} else {
			return Collections.emptyList();
		}
	}

}

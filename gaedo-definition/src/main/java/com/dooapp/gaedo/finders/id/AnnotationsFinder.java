package com.dooapp.gaedo.finders.id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.dooapp.gaedo.extensions.hierarchy.Child;
import com.dooapp.gaedo.extensions.hierarchy.Parent;
import com.dooapp.gaedo.properties.Property;

/**
 * Locate various annotations in source code
 * 
 * @author ndx
 * 
 */
public class AnnotationsFinder {
	public static enum Annotations {
		/**
		 * Define element as an id one
		 */
		ID(Arrays.asList("javax.persistence.Id", Id.class.getCanonicalName())),
		/**
		 * Define element as non-persisted
		 */
		TRANSIENT(Arrays.asList("javax.persistence.Transient")),
		/**
		 * Define element as parent one
		 */
		PARENT(
				Arrays.asList(Parent.class.getCanonicalName())),
		/**
		 * Define element as child one
		 */
		CHILD(Arrays
				.asList(Child.class.getCanonicalName()));

		private Annotations(Collection<String> names) {
			this.annotationsNames = Collections.unmodifiableCollection(names);
		}

		private Collection<String> annotationsNames;

		/**
		 * 
		 * @param annotations
		 * @return true if this particular annotation list has any of the input
		 *         annotations
		 */
		public boolean hasAny(Collection<? extends Annotation> annotations) {
			for (Annotation a : annotations) {
				if (annotationsNames.contains(a.annotationType().getName()))
					return true;
			}
			return false;
		}

		/**
		 * Check if property has the given annotation type
		 * @param p property to check
		 * @return true if property contains any of the given annotations
		 */
		public boolean is(Property p) {
			return hasAny(p.getAnnotations());
		}
	}

	/**
	 * Find the first field in class that has the id annotation
	 * 
	 * @param fields
	 *            collection of fields to lookup
	 * @return null if none found
	 */
	public static List<Property> findAll(Property[] fields,
			Annotations toSupport) {
		List<Property> returned = new LinkedList<Property>();
		for (Property f : fields) {
			// only non static fields are used
			if (!f.hasModifier(Modifier.STATIC)) {
				Collection<? extends Annotation> annotations = f
						.getAnnotations();
				if (toSupport.hasAny(annotations)) {
					returned.add(f);
					break;
				}
			}
		}
		return returned;
	}
}

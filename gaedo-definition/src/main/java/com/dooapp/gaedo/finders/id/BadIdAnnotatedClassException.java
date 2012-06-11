/**
 * 
 */
package com.dooapp.gaedo.finders.id;

import java.util.Collection;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;

/**
 * That class various id elements does not allow the correct definition of a simple id :
 * <ul>
 * <li>Either the class has no field annotated with one of the {@link Annotations.ID} possible values</li>
 * <li>Or the class has too many elements with such annotations</li>
 * <li>Or the class has only one field with such annotation, which is good, but the field is not correctly typed</li>
 * </ul>
 * As a remainder, for a class to be persisted using GAE, it must have as id field something like
 * <pre>
 * 		@Id
 * 		private long use_any_name_you_want_here;
 * </pre>
 * the name id is irrelevant here. However, the annotation and the type are.
 * @author Nicolas
 *
 */
public class BadIdAnnotatedClassException extends CrudServiceException {
	private static String toString(Collection<Class> expectedIdClasses) {
		StringBuilder sOut = new StringBuilder();
		if(expectedIdClasses!=null) {
			for(Class<?> c : expectedIdClasses) {
				sOut.append("\n\t").append(c.getCanonicalName());
			}
		}
		return sOut.toString();
	}

	public BadIdAnnotatedClassException(Class<?> containedClass, Collection<Class> expectedIdClasses) {
		this("Class "+containedClass.getName()+" must have EXACTLY one field annotated with @Id of one of these types : "+toString(expectedIdClasses));
	}

	protected BadIdAnnotatedClassException(String message) {
		super(message);
	}

	public BadIdAnnotatedClassException(Class<?> containedClass) {
		this("Class "+containedClass.getName()+" must have EXACTLY one field annotated with @Id");
	}
	
	
}
package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

public class UnableToCreateFieldInformerException extends CrudServiceException {

	public UnableToCreateFieldInformerException(Class<?> clazz,
			Class<? extends FieldInformer> informerClass,
			Class<? extends Property> class1, Exception e) {
		super("unable to create a field informer from field class "+clazz.getCanonicalName()+" which is associated to informer class "+informerClass.getName()+"\n" +
				"the most probable reason is that field informer class ("+informerClass.getName()+") does not declares a constructor having as parameter a Property object.", e);
	}

}

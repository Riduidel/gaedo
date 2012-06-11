package com.dooapp.gaedo.finders.repository;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.root.FieldInformerLocator;
import com.dooapp.gaedo.properties.Property;

/**
 * Field locator using a service repository to find the correct field informer
 * 
 * @author Nicolas
 * 
 */
public class ServiceBackedFieldLocator implements FieldInformerLocator {
	/**
	 * Service repository used to locate the {@link Informer} that will be
	 * served as {@link FieldInformer}
	 */
	private final ServiceRepository repository;

	public ServiceBackedFieldLocator(ServiceRepository repository) {
		super();
		this.repository = repository;
	}

	/*
	 * For a strange reason compiling this with maven fails with a mysterious
	 * message : [ERROR]
	 * /home/www/.hudson/jobs/Gaedo/workspace/trunk/gaedo-definition
	 * /src/main/java
	 * /com/dooapp/gaedo/finders/repository/ServiceBackedFieldLocator
	 * .java:[30,71] incompatible types; inferred type argument(s)
	 * com.dooapp.gaedo.finders.Informer<?> do not conform to bounds of type
	 * variable(s) InformerType found :
	 * <InformerType>com.dooapp.gaedo.finders.FinderCrudService<capture#225 of
	 * ?,InformerType> required: com.dooapp.gaedo.finders.FinderCrudService<?,?
	 * extends com.dooapp.gaedo.finders.Informer<?>>
	 * 
	 * Fast and ugly solution was obviously to get rid of generics
	 */
	@SuppressWarnings("unchecked")
	@Override
	public FieldInformer getInformerFor(Property field) {
		try {
			FinderCrudService service = repository.get(field.getType());
			return service.getInformer().asField(field);
		} catch (NoSuchServiceException e) {
			return null;
		}
	}

}

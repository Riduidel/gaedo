package com.dooapp.gaedo.finders.repository;

import java.lang.reflect.Type;
import java.util.Map;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;

/**
 * A service repository allows one to define which services are used for which bean and informer classes, and later to use these services to persist objects of the given classes.
 * Notice the service repository can also be viewed as a {@link Map} of classes to associated services. Unfortunatly (well, unfortunatly, only for the most hackerish users)
 * this is a read-only map.
 * @author Nicolas
 *
 */
public interface ServiceRepository extends Map<Class<?>, FinderCrudService<?, ?>> {
	/**
	 * Check if service repository contains the given class
	 * @param dataType
	 * @return well, you know how it works for maps ?
	 */
	public boolean containsKey(Class<?> dataType);
	/**
	 * Adds a service to this repository
	 * @param <DataType> managed data type
	 * @param <InformerType> informer type
	 * @param service service to add
	 */
	public <DataType, InformerType extends Informer<DataType>> void add(FinderCrudService<DataType, InformerType> service);
	
	/**
	 * Get service associated to the given data type
	 * @param <DataType> data type 
	 * @param <InformerType> informer type associated to this data type
	 * @param dataType data type class
	 * @return service associated o data type
	 * @throws NoSuchServiceException if no service exists for this data type
	 */
	public <DataType, InformerType extends Informer<DataType>> FinderCrudService<DataType, InformerType> get(Class<DataType> dataType);
	
	/**
	 * Guess which service should be used for a given data. As an example, when facing a Collection type, it will check if inner data is typed. If so, it will use inner data type.
	 * This one method is a best guess method, able to provide *very* weird error results
	 * @param type any type
	 * @return maybe a service, if one can be found.
	 * @throws NoSuchServiceException if no service exists for this data type
	 */
	public FinderCrudService<?, Informer<?>> get(Type type);
	
	/**
	 * Get property change support used for repository
	 * @return
	 */
	public PropertyChangeEmitter getSupport();
}

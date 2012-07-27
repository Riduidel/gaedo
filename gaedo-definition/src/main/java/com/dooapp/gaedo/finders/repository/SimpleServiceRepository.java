package com.dooapp.gaedo.finders.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;
import com.dooapp.gaedo.utils.PropertyChangeEmitterImpl;

/**
 * Simple implementation providing default overridable operations 
 * @author Nicolas
 *
 */
public class SimpleServiceRepository implements ServiceRepository {
	/**
	 * Collection of already known services
	 */
	private Map<Class<?>, FinderCrudService<?, ?>> services = new HashMap<Class<?>, FinderCrudService<?, ?>>();
	
	/**
	 * Unmodifiable view of services, used for all map methods
	 */
	private Map<Class<?>, FinderCrudService<?, ?>>  delegateMap = Collections.unmodifiableMap(services);
	
	private PropertyChangeEmitterImpl support;
	
	public boolean containsKey(Class<?> arg0) {
		return services.containsKey(arg0);
	}

	@Override
	public <DataType, InformerType extends Informer<DataType>> void add(
			FinderCrudService<DataType, InformerType> service) {
		services.put(service.getContainedClass(), service);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <DataType, InformerType extends Informer<DataType>> FinderCrudService<DataType, InformerType> get(
			Class<DataType> dataType) {
		if(services.containsKey(dataType))
			// Sometimes, those generics issues are just too annoying
			return (FinderCrudService<DataType, InformerType>) services.get(dataType);
		else
			throw new NoSuchServiceException("class "+dataType.getName()+" appears as not managed by a service of this repository");
	}

	@Override
	public PropertyChangeEmitter getSupport() {
		if(support==null) {
			support = new PropertyChangeEmitterImpl();
		}
		return support;
	}

	/**
	 * Try to find the best matching service
	 */
	@Override
	@SuppressWarnings("unchecked")
	public FinderCrudService<?, Informer<?>> get(Type type) {
		if(type instanceof Class) {
			return get((Class) type);
		} else if(type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			Type raw = pType.getRawType();
			if(raw instanceof Class) {
				Class rawClass = (Class) raw;
				if(Collection.class.isAssignableFrom(rawClass)) {
					return get(pType.getActualTypeArguments()[0]);
				}
			}
		}
		throw new NoSuchServiceException("class "+type.toString()+" appears as not managed by a service of this repository");
	}

	public void clear() {
		delegateMap.clear();
	}

	public boolean containsKey(Object key) {
		return delegateMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return delegateMap.containsValue(value);
	}

	public Set<java.util.Map.Entry<Class<?>, FinderCrudService<?, ?>>> entrySet() {
		return delegateMap.entrySet();
	}

	public FinderCrudService<?, ?> get(Object key) {
		if(key instanceof Class) {
			return (FinderCrudService<?, ?>) get((Class) key);
		} else {
			return (FinderCrudService<?, ?>) get(key.getClass());
		}
	}

	public int hashCode() {
		return delegateMap.hashCode();
	}

	public boolean isEmpty() {
		return delegateMap.isEmpty();
	}

	public Set<Class<?>> keySet() {
		return delegateMap.keySet();
	}

	/**
	 * @see #add(FinderCrudService) which is invoked in fact
	 */
	public FinderCrudService<?, ?> put(Class<?> key,
			FinderCrudService<?, ?> value) {
		add(value);
		return value;
	}

	public void putAll(
			Map<? extends Class<?>, ? extends FinderCrudService<?, ?>> m) {
		for(FinderCrudService<?, ?> elt : m.values())
			add(elt);
	}

	public FinderCrudService<?, ?> remove(Object key) {
		return delegateMap.remove(key);
	}

	public int size() {
		return delegateMap.size();
	}

	public Collection<FinderCrudService<?, ?>> values() {
		return delegateMap.values();
	}

}

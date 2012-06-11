package com.dooapp.gaedo.finders.repository;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;

/**
 * Subclass of simple service repository allowing subclasses usage
 * @author ndx
 *
 */
public class InheriterRepository extends SimpleServiceRepository {
	/**
	 * Override parent method to perform superclass lookup
	 * @param arg0
	 * @return
	 * @see com.dooapp.gaedo.finders.repository.SimpleServiceRepository#containsKey(java.lang.Class)
	 */
	@Override
	public boolean containsKey(Class<?> arg0) {
		if(super.containsKey(arg0))
			return true;
		else {
			if(arg0==null) {
				return false;
			} else if(!Object.class.equals(arg0)) {
				return containsKey(arg0.getSuperclass());
			} else {
				return false;
			}
		}
	}
	
	@Override
	public <DataType, InformerType extends Informer<DataType>> FinderCrudService<DataType, InformerType> get(Class<DataType> dataType) {
		return get(dataType, dataType);
	}

	public <DataType, InformerType extends Informer<DataType>> FinderCrudService<DataType, InformerType> get(Class<?> dataType, Class<DataType> original) {
		if(super.containsKey(dataType))
			return super.get((Class<DataType>)dataType);
		else {
			if(!Object.class.equals(dataType)) {
				return get(dataType.getSuperclass(), original);
			} else {
				throw new NoSuchServiceException("class "+original.getName()+" appears as not managed by a service of this repository");
			}
		}
	}
}

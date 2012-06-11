package com.dooapp.gaedo.finders.dynamic;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;

/**
 * Base interface for all dynamic fidners. Notice that if you want your dynamic servcie to have id based
 * operations, you have to also extend the IdBasedServcie interface
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public interface DynamicFinder<DataType, InformerType extends Informer<DataType>>
		extends FinderCrudService<DataType, InformerType> {

}

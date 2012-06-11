package com.dooapp.gaedo.finders.dynamic;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;

/**
 * From a dynamic interface and a backend implementation, implementors of this interface are able to generate an implementor of this interface
 * @author ndx
 *
 */
public interface ServiceGenerator {
	/**
	 * Factory method creating the required obejct from the given interface and back-end implementor
	 * @param <DataType> type of data managed by this service
	 * @param <InformerType> informer used to provide informations about data
	 * @param <Implementation> returned implementation
	 * @param toImplement class of Implementation
	 * @param backEnd back end that MUST receive all translated calls
	 * @return an object implementing Implementation and redirecting decorated calls to backEnd
	 */
	<DataType, InformerType extends Informer<DataType>, Implementation extends DynamicFinder<DataType, InformerType>> Implementation generate(
			Class<Implementation> toImplement,
			FinderCrudService<DataType, InformerType> backEnd);
}

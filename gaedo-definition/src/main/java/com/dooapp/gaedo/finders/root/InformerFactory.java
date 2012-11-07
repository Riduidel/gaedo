package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.finders.Informer;

/**
 * Interface allowing dynamic finding of informer associated to given bean class
 * @author ndx
 *
 */
public interface InformerFactory {

	/**
	 * Get an informer of a given class from the given contained type. If there is a way to construct such an informer, it is this class's job to do such a work
	 * @param <InformerType> expected informer type
	 * @param <ContainedType> data type
	 * @param informerClass expected informer type
	 * @param containedType data type
	 * @return
	 */
	public <InformerType extends Informer<ContainedType>, ContainedType> InformerType get(Class<InformerType> informerClass, Class<ContainedType> containedType);

}

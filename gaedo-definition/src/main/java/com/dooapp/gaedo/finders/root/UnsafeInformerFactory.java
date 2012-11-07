package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.finders.Informer;

/**
 * A subclass of informer factory providing type-unsafe informer creation (or sorta)
 * @author ndx
 *
 */
public abstract class UnsafeInformerFactory extends AbstractInformerFactory {

	public UnsafeInformerFactory(
			ReflectionBackedInformerFactory reflectiveInformerFactory) {
		super(reflectiveInformerFactory);
	}

	/**
	 * This implementation delegates calls to an unsafe method
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <InformerType extends Informer<ContainedType>, ContainedType> InformerType createDynamicInformer(
			Class<InformerType> informerClass,
			Class<ContainedType> containedType,
			Informer<ContainedType> existingInformer) {
		Object returned = createUnsafeDynamicInformer(informerClass, containedType, existingInformer);
		return informerClass.cast(returned);
	}

	/**
	 * This method allows type unsafe creation of informer. notice there will be absolutely no check done on class consistency after creation.
	 * So use it with great care (no i won't quote spiderman here, it's already a big enough net).
	 * @param <InformerType>
	 * @param <ContainedType>
	 * @param informerClass
	 * @param containedType
	 * @param existingInformer
	 * @return
	 */
	protected abstract <InformerType extends Informer<ContainedType>, ContainedType> Object createUnsafeDynamicInformer(Class<InformerType> informerClass,
			Class<ContainedType> containedType,
			Informer<ContainedType> existingInformer);
}

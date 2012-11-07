package com.dooapp.gaedo.finders.root;

import java.util.HashMap;
import java.util.Map;

import com.dooapp.gaedo.finders.Informer;

/**
 * A base class allowing easy definition of a Map of extended informers
 * 
 * @author ndx
 * 
 */
public abstract class AbstractInformerFactory {

	/**
	 * Map of loaded informers. Notice contained informers are often more "intelligent" instances than raw {@link Informer} (typically proxies)
	 */
	protected Map<Class, Informer> loaded = new HashMap();
	/**
	 * Basic informer factory, to provide raw {@link Informer} for input classes
	 */
	protected final ReflectionBackedInformerFactory reflectiveInformerFactory;

	public AbstractInformerFactory(
			ReflectionBackedInformerFactory reflectiveInformerFactory) {
		super();
		this.reflectiveInformerFactory = reflectiveInformerFactory;
	}

	/**
	 * Lazy load all Informer class corresponding to given class name and for
	 * the given type
	 * 
	 * @param <InformerType>
	 * @param <ContainedType>
	 * @param informerClass
	 *            expected informer class
	 * @param containedType
	 *            contained data type
	 * @return
	 */
	public <InformerType extends Informer<ContainedType>, ContainedType> InformerType get(
			Class<InformerType> informerClass,
			Class<ContainedType> containedType) {
		if (!loaded.containsKey(informerClass)) {
			synchronized (this) {
				if (!loaded.containsKey(informerClass)) {
					Informer<ContainedType> reflectiveInformer = reflectiveInformerFactory.get(containedType);
					InformerType dynamicInformer = createDynamicInformer(informerClass, containedType, reflectiveInformer);
					loaded.put(informerClass,
							dynamicInformer);
					// Separation of loading and validation allows circular references
					if(dynamicInformer instanceof Validable) {
						((Validable) dynamicInformer).validate();
					}
				}
			}
		}
		return (InformerType) loaded.get(informerClass);
	}

	/**
	 * Method to which instanciation of effective informers is delegated.
	 * implementors of this method are responsible for returning objects implementing informerClass interface by ma
	 * 
	 * @param <InformerType>
	 * @param <ContainedType>
	 * @param informerClass informer class that must be implemented
	 * @param containedType contained data type
	 * @param existingInformer existing informer implementation to which calls can be redirected
	 * @return
	 */
	protected abstract <InformerType extends Informer<ContainedType>, ContainedType> InformerType createDynamicInformer(
			Class<InformerType> informerClass,
			Class<ContainedType> containedType, 
			Informer<ContainedType> existingInformer);

	/**
	 * @return the reflectiveInformerFactory
	 * @category getter
	 * @category reflectiveInformerFactory
	 */
	public ReflectionBackedInformerFactory getReflectiveInformerFactory() {
		return reflectiveInformerFactory;
	}
}

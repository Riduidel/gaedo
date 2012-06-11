package com.dooapp.gaedo.finders.collections;

import java.util.List;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;

public class DefaultCollectionBackedFinderService<DataType extends Object, InformerType extends Informer<DataType>> extends
		CollectionBackedFinderService<DataType, InformerType> implements
		FinderCrudService<DataType, InformerType> {

	/**
	 * Create a FinderCrudService for given type class and informer class. Notice that if type class reveals to have an id field, we will use a IdentifiableCollectionBackedFinderServcie
	 * @param <Type> informed type
	 * @param <InformerType> informer for the upper
	 * @param typeClass class of informed type
	 * @param informerTypeClass class of informer type
	 * @return an instance of {@link CollectionBackedFinderService}
	 * @deprecated replaced by {@link #create(Class, Class, ProxyBackedInformerFactory, PropertyProvider)}
	 */
	@Deprecated()
	public static <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> create(
			Class<Type> typeClass, Class<InformerType> informerTypeClass, InformerFactory factory) {
		return create(typeClass, informerTypeClass, factory, new FieldBackedPropertyProvider());
	}

	/**
	 * Create a FinderCrudService for given type class and informer class. Notice that if type class reveals to have an id field, we will use a IdentifiableCollectionBackedFinderServcie
	 * @param <Type> informed type
	 * @param <InformerType> informer for the upper
	 * @param typeClass class of informed type
	 * @param informerTypeClass class of informer type
	 * @param provider property provider used to allow id detection
	 * @return an instance of {@link CollectionBackedFinderService}
	 */
	public static <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> create(
			Class<Type> typeClass, Class<InformerType> informerTypeClass, InformerFactory factory, PropertyProvider provider) {
		List<Property> ids = AnnotationUtils.locateAllFields(provider, typeClass, Annotations.ID);
		if(ids.size()>0)
			return new IdSupportingCollectionBackedFinderService<Type, InformerType>(typeClass, informerTypeClass, factory, ids);
		else
			return new DefaultCollectionBackedFinderService<Type, InformerType>(typeClass, informerTypeClass, factory);
	}


	public DefaultCollectionBackedFinderService(Class<DataType> containedClass,
			Class<InformerType> informerClass,
			InformerFactory factory) {
		super(containedClass, informerClass, factory);
	}
}

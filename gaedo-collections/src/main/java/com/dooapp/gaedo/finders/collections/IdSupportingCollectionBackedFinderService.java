package com.dooapp.gaedo.finders.collections;

import java.util.Collection;
import java.util.List;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.id.IdBasedServiceHelper;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.properties.Property;

/**
 * Collection service providing id lookup capabilities
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public class IdSupportingCollectionBackedFinderService<DataType, InformerType extends Informer<DataType>> extends
		DefaultCollectionBackedFinderService<DataType, InformerType> implements
		FinderCrudService<DataType, InformerType>, IdBasedService<DataType> {

	private IdBasedServiceHelper<DataType, InformerType> idHelper;

	public IdSupportingCollectionBackedFinderService(
			Class<DataType> containedClass, Class<InformerType> informerClass,
			InformerFactory factory, List<Property> idProperties) {
		super(containedClass, informerClass, factory);
		this.idHelper = new IdBasedServiceHelper<DataType, InformerType>(idProperties, this);
	}

	@Override
	public DataType findById(final Object... id) {
		return idHelper.findById(id);
	}

	@Override
	public Collection<Property> getIdProperties() {
		return idHelper.getIdProperties();
	}

	@Override
	public boolean assignId(DataType value, Object... id) {
		return idHelper.assignId(value, id);
	}

}

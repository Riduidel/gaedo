package com.dooapp.gaedo.finders.id;

import java.util.Collection;
import java.util.List;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.properties.Property;

/**
 * Helper class simplifying the writing of an IdBasedService by providing some helper methods
 * @author ndx
 *
 */
public class IdBasedServiceHelper<DataType, InformerType extends Informer<DataType>> {

	private List<Property> idProperties;
	private FinderCrudService<DataType, InformerType> backEnd;

	public IdBasedServiceHelper(List<Property> idProperties,
					FinderCrudService<DataType, InformerType> backEnd) {
		this.idProperties = idProperties;
		this.backEnd = backEnd;
	}
	
	public DataType findById(final Object... id) {
		return backEnd.find().matching(new QueryBuilder<InformerType>() {

			@Override
			public QueryExpression createMatchingExpression(
					InformerType informer) {
				
				QueryExpression returned = null;
				for(int index=0; index<id.length; index++) {
					FieldInformer fieldInformer = backEnd.getInformer().get(idProperties.get(index).getName());
					QueryExpression propertyEquality = fieldInformer.equalsTo(id[index]);
					if(returned==null) {
						returned = propertyEquality;
					} else {
						returned = Expressions.and(returned, propertyEquality);
					}
				}
				return returned;
			}
		}).getFirst();
	}

	public Collection<Property> getIdProperties() {
		return idProperties;
	}

	public boolean assignId(DataType value, Object... id) {
		for(int index=0; index<id.length; index++) {
			idProperties.get(index).set(value, id[index]);
		}
		return true;
	}
}

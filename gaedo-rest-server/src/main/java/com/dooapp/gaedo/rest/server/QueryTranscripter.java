package com.dooapp.gaedo.rest.server;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.AggregatingQueryExpression;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;

/**
 * Transform some http query parameters into a gaedo query statement
 * @author ndx
 *
 */
public class QueryTranscripter {
	private static Logger logger = Logger.getLogger(QueryTranscripter.class.getName());
	/**
	 * @param service
	 * @param filterParams
	 * @return
	 */
	public <DataType, InformerType extends Informer<DataType>> QueryBuilder<InformerType> buildQuery(FinderCrudService<DataType, InformerType> service,
			Map<String, Object> filterParams) {
		Map<String, Object> valuesAsTree = Utils.getValuesAsTree(filterParams);
		// Now explore tree to build a QueryBuilder instance
		final AndQueryExpression combinator = new AndQueryExpression();
		for(Map.Entry<String, Object> entry : valuesAsTree.entrySet()) {
			buildQueryExpression(combinator, service.getInformer(), (Map<String, Object>) entry.getValue());
		}
		return new QueryBuilder<InformerType>() {

			@Override
			public QueryExpression createMatchingExpression(
					InformerType informer) {
				return combinator;
			}
		};
	}
	
	public void buildQueryExpression(AggregatingQueryExpression parent, FieldInformer fieldInformer,
			Map<String, Object> value) {
		if(FilterAggregator.isAggregationMap(value)) {
			FilterAggregator current = FilterAggregator.get(value);
			AggregatingQueryExpression expr = current.createExpression();
			parent.add(expr);
			buildQueryExpression(expr, fieldInformer, (Map<String, Object>) value.get(current.getKey()));
		} else {
			for(Map.Entry<String, Object> entry : value.entrySet()) {
				if(fieldInformer instanceof Informer) {
					Informer beanInformer = (Informer) fieldInformer;
					try {
						FieldInformer usedInformer = beanInformer.get(entry.getKey());
						buildQueryExpression(parent, usedInformer, (Map<String, Object>) entry.getValue());
					} catch(Exception e) {
						logger.log(Level.FINE, "unable to build query statement from field \""+entry.getKey()+"\"", e);
					}
				} else {
					// Locate method by name
					Method toCall = com.dooapp.gaedo.utils.Utils.getNameMap(fieldInformer.getClass().getMethods()).get(entry.getKey());
					try {
						parent.add((QueryExpression) toCall.invoke(fieldInformer, fieldInformer.getField().fromString(entry.getValue().toString())));
					} catch (Exception e) {
						logger.log(Level.FINE, "unable to build query statement from field "+fieldInformer+" and method "+entry.getKey(), e);
					}
				}
			}
		}
	}

}

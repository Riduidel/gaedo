package com.dooapp.gaedo.rest.server;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;

/**
 * Transcript some query parameters into a sorting operation
 * @author ndx
 *
 */
public class SortingTranscripter {
	private static final Logger logger = Logger.getLogger(SortingTranscripter.class.getName());
	
	public <InformerType extends Informer<?>> SortingBuilder<InformerType> buildSorting(FinderCrudService service,
			Map<String, Object> sortParams) {
		Map<String, Object> valuesAsTree = Utils.getValuesAsTree(sortParams);
		// Now explore tree to build a QueryBuilder instance
		final SortingExpressionImpl combinator = new SortingExpressionImpl();
		for(Map.Entry<String, Object> entry : valuesAsTree.entrySet()) {
			buildSortingExpression(combinator, service.getInformer(), (Map<String, Object>) entry.getValue());
		}
		return new SortingBuilder<InformerType>() {

			@Override
			public SortingExpression createSortingExpression(
					InformerType informer) {
				return combinator;
			}
		};
	}

	private void buildSortingExpression(SortingExpressionImpl parent,
			FieldInformer fieldInformer, Map<String, Object> value) {
		for(Map.Entry<String, Object> entry : value.entrySet()) {
			if(fieldInformer instanceof Informer) {
				Informer beanInformer = (Informer) fieldInformer;
				try {
					FieldInformer usedInformer = beanInformer.get(entry.getKey());
					parent.add(usedInformer, SortingExpression.Direction.valueOf(entry.getValue().toString()));
				} catch(Exception e) {
					logger.log(Level.FINE, "unable to create a sorting expression from field name "+entry.getKey()+" in class "+beanInformer.toString(), e);
				}
			}
		}
	}

}

package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.finders.Finder;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;

/**
 * Ultra-simple finder class
 *
 * @author ndx
 *
 */
public class SimpleFinder<DataType, InformerType extends Informer<DataType>>
		implements Finder<DataType, InformerType> {
	/**
	 * Source service, used to generate the query statement
	 */
	private AbstractFinderService<DataType, InformerType> service;

	/**
	 * Constructs finder from service
	 * @param abstractFinderService
	 */
	public SimpleFinder(
			AbstractFinderService<DataType, InformerType> abstractFinderService) {
		service = abstractFinderService;
	}

	/**
	 * Build a query statement from the input query builder provided. Notice that we provide the query a maybe not unique id
	 */
	public QueryStatement<DataType, DataType, InformerType> matching(QueryBuilder<? super InformerType> query) {
		QueryStatement<DataType, DataType, InformerType> returned = service.createQueryStatement(query);
		StackTraceElement creator = new Throwable().getStackTrace()[1];
		returned.setId(creator.getClassName()+" "+creator.getMethodName());
		return returned;
	}

}
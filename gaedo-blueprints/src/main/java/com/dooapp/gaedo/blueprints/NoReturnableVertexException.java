package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.finders.QueryExpression;

public class NoReturnableVertexException extends BluePrintsCrudServiceException {

	public NoReturnableVertexException(QueryExpression filterExpression) {
		super("there is no way to find a simple entity (as returnable by DatastoreService#getEntity) using the given query\n"+filterExpression);
	}

}

package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.exceptions.BadQueryResultException;
import com.dooapp.gaedo.finders.QueryExpression;

public class NoReturnableEntity extends BadQueryResultException {

	public NoReturnableEntity(QueryExpression expression) {
		super("there is no way to find a simple entity (as returnable by DatastoreService#getEntity) using the given query\n"+expression);
	}

}

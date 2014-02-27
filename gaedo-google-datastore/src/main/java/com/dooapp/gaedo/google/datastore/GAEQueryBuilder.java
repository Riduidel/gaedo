package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.SortingExpression.Direction;
import com.dooapp.gaedo.finders.expressions.AbstractBasicExpression;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.AnythingExpression;
import com.dooapp.gaedo.finders.expressions.CollectionContaingExpression;
import com.dooapp.gaedo.finders.expressions.ContainsStringExpression;
import com.dooapp.gaedo.finders.expressions.EndsWithExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.finders.expressions.EqualsToIgnoreCaseExpression;
import com.dooapp.gaedo.finders.expressions.GreaterThanExpression;
import com.dooapp.gaedo.finders.expressions.InstanceOfExpression;
import com.dooapp.gaedo.finders.expressions.LowerThanExpression;
import com.dooapp.gaedo.finders.expressions.MapContainingKeyExpression;
import com.dooapp.gaedo.finders.expressions.MatchesRegexpExpression;
import com.dooapp.gaedo.finders.expressions.NotQueryExpression;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;
import com.dooapp.gaedo.finders.repository.NoSuchServiceException;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

public class GAEQueryBuilder<DataType> implements QueryExpressionVisitor {
	private static final Logger logger = Logger.getLogger(GAEQueryBuilder.class
			.getName());

	private static final String KEY_PROPERTY = "__key__";

	/**
	 * Set to true to reverse condition.
	 */
	private boolean not;

	/**
	 * Query executed in case of effective search upon various fields. Notice
	 * this query is not final since it may be replaced in case of
	 * {@link CollectionContaingExpression} queries
	 */
	private Query query;
	/**
	 * Id manager is used to detect queries on id field
	 */
	private final IdManager idManager;

	/**
	 * Service repository used to get id for fields
	 */
	private ServiceRepository repository;

	/**
	 * Expected result kind. It allows for result translation when doing queries
	 * on collections
	 */
	private String expectedKind;

	/**
	 * Creates visitor with required elements
	 *
	 * @param defaultKind
	 *            default kind used to create the {@link #query} object. See
	 *            this object's doc for more details about variable kind.
	 * @param idManager
	 *            identitiy field manager, used for query id mapping
	 * @param repository
	 *            service repository, used for value transformation
	 */
	public GAEQueryBuilder(String defaultKind, IdManager idManager,
			ServiceRepository repository) {
		this.expectedKind = defaultKind;
		this.query = new Query(defaultKind);
		this.idManager = idManager;
		this.repository = repository;
	}

	/**
	 * Get optimal query for finding results as fast as possible
	 *
	 * @param datastore
	 *            used datastore
	 * @param sortingExpression
	 *            sorting expression used to organize results
	 * @return one of {@link ClassicalQuery} or ??? depending upon the best
	 *         executor for query
	 */
	public DataStoreExecutableQuery getQuery(DatastoreService datastore,
			SortingExpression sortingExpression) {
		boolean onlyOnKeys = true;
		Collection<Key> keys = new LinkedList<Key>();
		for (FilterPredicate predicate : query.getFilterPredicates()) {
			onlyOnKeys &= (KEY_PROPERTY.equals(predicate.getPropertyName()) && Query.FilterOperator.EQUAL
					.equals(predicate.getOperator()));
			if (onlyOnKeys)
				keys.add((Key) predicate.getValue());
		}
		if (onlyOnKeys) {
			// If there is a non-empty sorting expression on this key query,
			// there is a problem
			if (sortingExpression != null
					&& sortingExpression.iterator().hasNext()) {
				logger.log(Level.WARNING,
						"unable to sort queries based on key only",
						new Exception());
			}
			return new KeysQueryLookup(datastore, keys, expectedKind);
		}
		if (sortingExpression != null) {
			for (Entry<FieldInformer, SortingExpression.Direction> entry : sortingExpression) {
				query
						.addSort(
								Utils.getDatastoreFieldName(entry.getKey()
										.getField()),
								entry.getValue() == Direction.Ascending ? SortDirection.ASCENDING
										: SortDirection.DESCENDING);
			}
		}
		return new ClassicalQuery(datastore, query);
	}

	@Override
	public void endVisit(OrQueryExpression orQueryExpression) {
	}

	@Override
	public void endVisit(AndQueryExpression andQueryExpression) {
	}

	@Override
	public void endVisit(NotQueryExpression notQueryExpression) {
		not = false;
	}

	@Override
	public void startVisit(OrQueryExpression expression) {
		throw new UnsupportedOperationException(expression.getClass().getName()
				+ " is not supported. For more details, ask Google ");
	}

	@Override
	public void startVisit(AndQueryExpression andQueryExpression) {
	}

	@Override
	public void startVisit(NotQueryExpression notQueryExpression) {
		not = true;

	}

	@Override
	public void visit(AnythingExpression expression) {
		query.addFilter(getQueryFieldName(expression),
				Query.FilterOperator.NOT_EQUAL, null);
	}

	@Override
	public void visit(EqualsExpression expression) {
		query.addFilter(getQueryFieldName(expression),
				not ? Query.FilterOperator.NOT_EQUAL
						: Query.FilterOperator.EQUAL, resolve(expression
						.getField(), expression.getValue()));
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(GreaterThanExpression<ComparableType> expression) {
		if (not) {
			throw new UnsupportedOperationException(expression.getClass()
					.getName()
					+ " is not supported. For more details, ask Google ");
		}
		query.addFilter(getQueryFieldName(expression),
				expression.isStrictly() ? Query.FilterOperator.GREATER_THAN
						: Query.FilterOperator.GREATER_THAN_OR_EQUAL, resolve(
						expression.getField(), expression.getValue()));
	}

	@Override
	public <ComparableType extends Comparable<ComparableType>> void visit(LowerThanExpression<ComparableType> expression) {
		if (not) {
			throw new UnsupportedOperationException(expression.getClass()
					.getName()
					+ " is not supported. For more details, ask Google ");
		}
		query.addFilter(getQueryFieldName(expression),
				expression.isStrictly() ? Query.FilterOperator.LESS_THAN
						: Query.FilterOperator.LESS_THAN_OR_EQUAL, resolve(
						expression.getField(), expression.getValue()));
	}

	@Override
	public void visit(EqualsToIgnoreCaseExpression expression) {
		throw new UnsupportedOperationException(expression.getClass()
				.getName()
				+ " is not supported. For more details, ask Google ");
	}

	@Override
	public void visit(MatchesRegexpExpression expression) {
		throw new UnsupportedOperationException(expression.getClass()
						.getName()
						+ " is not supported. For more details, ask Google ");
	}

	@Override
	public void visit(ContainsStringExpression expression) {
		if (not) {
			throw new UnsupportedOperationException(expression.getClass()
					.getName()
					+ " is not supported. For more details, ask Google ");
		}
		query.addFilter(getQueryFieldName(expression), Query.FilterOperator.IN,
				resolve(expression.getField(), expression.getContained()));
	}

	/**
	 * Well, a weird trick :
	 * http://stackoverflow.com/questions/1554600/implementing
	 * -starts-with-and-ends-with-queries-with-google-app-engine
	 */
	@Override
	public void visit(StartsWithExpression expression) {
		if (not) {
			throw new UnsupportedOperationException(expression.getClass()
					.getName()
					+ " is not supported. For more details, ask Google ");
		}
		String queryFieldName = getQueryFieldName(expression);
		Object resolvedText = resolve(expression.getField(), expression
				.getStart());
		query.addFilter(queryFieldName,
				Query.FilterOperator.GREATER_THAN_OR_EQUAL, resolvedText)
				.addFilter(queryFieldName, Query.FilterOperator.LESS_THAN,
						resolvedText + "\ufffd");
	}

	/**
	 * From various sources, it does not seems to work (see
	 * http://stackoverflow.
	 * com/questions/1554600/implementing-starts-with-and-ends
	 * -with-queries-with-google-app-engine)
	 */
	@Override
	public void visit(EndsWithExpression expression) {
		// if (not) {
		// throw new UnsupportedOperationException(expression.getClass()
		// .getName()
		// + " is not supported. For more details, ask Google ");
		// }
		throw new UnsupportedOperationException(
				"ends with does not seems to be supported by GAE");
		// query.addFilter(getQueryFieldName(expression),
		// Query.FilterOperator.IN,
		// resolve(expression.getField(), expression.getEnd()));
	}

	@Override
	public void visit(InstanceOfExpression instanceOfExpression) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method QueryExpressionVisitor#visit has not yet been implemented AT ALL. In fact, I forgot how to do such things on GAE");
	}

	/**
	 * Get field name associated to query. This method also manages the id
	 * field, by replacing its classical name with the key column.
	 *
	 * @param expression
	 * @return
	 */
	private String getQueryFieldName(AbstractBasicExpression expression) {
		if (idManager.isIdField(expression.getField()))
			return KEY_PROPERTY;
		return Utils.getDatastoreFieldName(expression.getField());
	}

	/**
	 * Resolve value to translate keys
	 *
	 * @param field
	 *            input field, should be used for translating value in Key
	 * @param value
	 *            input value
	 * @return output value, as resolved if key
	 *
	 */
	/*
	 * Remove generic types to fix strange compilation failure with maven:
	 * gaedo/gaedo-google-datastore/src
	 * /main/java/org/gaedo/google/datastore/GAEQueryBuilder.java:[199,87]
	 * inconvertible types found :
	 * com.dooapp.gaedo.finders.FinderCrudService<capture#345 of
	 * ?,com.dooapp.gaedo.finders.Informer<capture#345 of ?>> required:
	 * org.gaedo.google.datastore.DatastoreFinderService<?,?>
	 */
	@SuppressWarnings("unchecked")
	private Object resolve(Property field, Object value) {
		if (idManager.isIdField(field))
			return idManager.buildKey(query.getKind(), value);
		try {
			DatastoreFinderService service = (DatastoreFinderService) repository
					.get(field.getGenericType());
			return service.getIdManager().getKey(service.getKind(), value);
		} catch (NoSuchServiceException e) {
			// Simple data, return it
			return value;
		}
	}

	/**
	 * This one is rather crazy. We will have to change the explored space to be
	 * the collection one, but ensure results are correctly resolved. To make
	 * long things short, this is a work in progress. To change explored space,
	 * we have no solution other than recreating query from scratch.
	 */
	@Override
	public void visit(CollectionContaingExpression expression) {
		Object searchedData = resolve(expression.getField(), expression
				.getContained());
		query = new Query(Utils.getDatastoreFieldName(expression.getField()));
		query.addFilter(Utils.COLLECTION_VALUE_PROPERTY,
				Query.FilterOperator.EQUAL, searchedData);
	}

	@Override
	public void visit(MapContainingValueExpression expression) {
		Object searchedData = resolve(expression.getField(), expression
				.getContained());
		query = new Query(Utils.getDatastoreFieldName(expression.getField()));
		query.addFilter(Utils.MAP_KEY_PROPERTY, Query.FilterOperator.EQUAL,
				searchedData);
	}

	@Override
	public void visit(MapContainingKeyExpression expression) {
		Object searchedData = resolve(expression.getField(), expression
				.getContained());
		query = new Query(Utils.getDatastoreFieldName(expression.getField()));
		query.addFilter(Utils.MAP_VALUE_PROPERTY, Query.FilterOperator.EQUAL,
				searchedData);
	}
}

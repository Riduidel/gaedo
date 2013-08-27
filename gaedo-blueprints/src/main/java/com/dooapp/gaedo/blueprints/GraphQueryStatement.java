package com.dooapp.gaedo.blueprints;

import java.beans.PropertyChangeEvent;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.queries.BluePrintsQueryBuilder;
import com.dooapp.gaedo.blueprints.queries.DataTypeIterable;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.exceptions.range.BadRangeDefinitionException;
import com.dooapp.gaedo.exceptions.range.BadStartIndexException;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitorAdapter;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.expressions.AndQueryExpression;
import com.dooapp.gaedo.finders.expressions.AnythingExpression;
import com.dooapp.gaedo.finders.expressions.CollectionContaingExpression;
import com.dooapp.gaedo.finders.expressions.ContainsStringExpression;
import com.dooapp.gaedo.finders.expressions.EndsWithExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.finders.expressions.GreaterThanExpression;
import com.dooapp.gaedo.finders.expressions.LowerThanExpression;
import com.dooapp.gaedo.finders.expressions.MapContainingKeyExpression;
import com.dooapp.gaedo.finders.expressions.NotQueryExpression;
import com.dooapp.gaedo.finders.expressions.OrQueryExpression;
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitor;
import com.dooapp.gaedo.finders.expressions.QueryExpressionVisitorAdapter;
import com.dooapp.gaedo.finders.expressions.StartsWithExpression;
import com.dooapp.gaedo.finders.informers.MapContainingValueExpression;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.tinkerpop.blueprints.Vertex;

public class GraphQueryStatement<
		ValueType,
		DataType,
		InformerType extends Informer<DataType>>
	implements QueryStatement<ValueType, DataType, InformerType> {

	protected QueryBuilder<InformerType> query;
	protected AbstractBluePrintsBackedFinderService<?, DataType, InformerType> service;
	protected ServiceRepository repository;

	public GraphQueryStatement(QueryBuilder<InformerType> query,
					AbstractBluePrintsBackedFinderService<?, DataType, InformerType> service,
					ServiceRepository repository) {
		this.query = query;
		this.service = service;
		this.repository = repository;
		this.state = State.INITIAL;
	}

	/**
	 * Query id, can be used for debugging
	 */
	private String id;
	/**
	 * Query execution state
	 */
	protected State state;
	/**
	 * Sorting expression used to define sort criterias
	 */
	private SortingExpression sortingExpression = new SortingExpressionImpl();
	private QueryExpression filterExpression;

	private GraphExecutableQuery prepareQuery() {
		try {
			if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "preparing query "+id);
			}
			BluePrintsQueryBuilder<DataType, InformerType> builder = new BluePrintsQueryBuilder<DataType, InformerType>(service);
			InformerType informer = service.getInformer();
			filterExpression = query.createMatchingExpression(informer);
			filterExpression.accept(builder);
			if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "filering expression for "+id+" is "+filterExpression);
			}
			return builder.getQuery(sortingExpression);
		} finally {
			if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "query "+id+" is constructed and usable ... now matching");
			}
			setState(State.MATCHING);
		}
	}

	@Override
	public int count() {
		try {
			return prepareQuery().count();
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public Iterable<ValueType> get(int start, int end) {
		try {
			if (start < 0) {
				throw new BadStartIndexException(start);
			} else if (end < start) {
				throw new BadRangeDefinitionException(start, end);
			}
			return createResultsIterable(prepareQuery().get(start, end));
		} finally {
			setState(State.EXECUTED);
		}
	}

	/**
	 * Create result iterable from the vertex iterables.
	 * @param iterable list of vertex to navigate
	 * @return
	 */
	private DataTypeIterable<DataType> createResultsIterable(Iterable<Vertex> iterable) {
		return new DataTypeIterable<DataType>(service, iterable, createPrepopulatedCache());
	}

	private ObjectCache createPrepopulatedCache() {
		final ObjectCache cache = ObjectCache.create(CascadeType.REFRESH);
		class CacheLoader extends QueryExpressionContainerVisitorAdapter {

			/**
			 * @param expression
			 * @see com.dooapp.gaedo.finders.expressions.QueryExpressionVisitorAdapter#visit(com.dooapp.gaedo.finders.expressions.EqualsExpression)
			 */
			@Override
			public void visit(EqualsExpression expression) {
				Object equalsValue = expression.getValue();
				putInCache(cache, equalsValue);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			private void putInCache(final ObjectCache cache, Object equalsValue) {
				if(equalsValue!=null) {
					// Object class is in repository, so try to find its object key to put it in cache
					Class<?> objectClass = equalsValue.getClass();
					if(repository.containsKey(objectClass)) {
						AbstractBluePrintsBackedFinderService service = (AbstractBluePrintsBackedFinderService) repository.get(objectClass);
						String objectId = service.getIdVertexId(equalsValue, false);
						cache.put(objectId, equalsValue);
					}
				}
			}

		}
		CacheLoader loader = new CacheLoader();
		filterExpression.accept(loader);
		sortingExpression.accept(loader);
		// Now cache is populated
		return cache;
	}

	@Override
	public Iterable<ValueType> getAll() {
		try {
			return createResultsIterable(prepareQuery().getAll());
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public ValueType getFirst() {
		try {
			Vertex result = prepareQuery().getVertex();
			if (result == null)
				throw new NoReturnableVertexException(filterExpression);
			return service.loadObject(result, createPrepopulatedCache());
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public QueryStatement<DataType, InformerType> sortBy(SortingBuilder<InformerType> expression) {
		try {
			this.sortingExpression = expression.createSortingExpression(service
					.getInformer());
			return this;
		} finally {
			setState(State.SORTING);
		}
	}

	@Override
	public void accept(QueryExpressionContainerVisitor visitor) {
		visitor.startVisit(this);
		filterExpression.accept(visitor);
		sortingExpression.accept(visitor);
		visitor.endVisit(this);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param state the state to set
	 * @category setter
	 * @category state
	 */
	public void setState(State state) {
		if(this.state!=state) {
			// Special construct allowing some very weird behaviour (like getting execution time for a query, I think)
			State old = this.state;
			this.state = state;
			repository.getSupport().firePropertyChange(
					new PropertyChangeEvent(this, QueryStatement.STATE_PROPERTY,
							old, state));
		}
	}

}

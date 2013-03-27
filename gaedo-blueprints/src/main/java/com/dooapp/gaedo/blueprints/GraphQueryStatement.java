package com.dooapp.gaedo.blueprints;

import java.beans.PropertyChangeEvent;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.queries.BluePrintsQueryBuilder;
import com.dooapp.gaedo.blueprints.queries.DataTypeIterable;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.exceptions.range.BadRangeDefinitionException;
import com.dooapp.gaedo.exceptions.range.BadStartIndexException;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.tinkerpop.blueprints.Vertex;

public class GraphQueryStatement<
		DataType, 
		InformerType extends Informer<DataType>> 
	implements QueryStatement<DataType, InformerType> {
	
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
	public Iterable<DataType> get(int start, int end) {
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
		return new DataTypeIterable<DataType>(service, iterable);
	}

	@Override
	public Iterable<DataType> getAll() {
		try {
			return createResultsIterable(prepareQuery().getAll());
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public DataType getFirst() {
		try {
			Vertex result = prepareQuery().getVertex();
			if (result == null)
				throw new NoReturnableVertexException(filterExpression);
			return service.loadObject(result, new TreeMap<String, Object>());
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

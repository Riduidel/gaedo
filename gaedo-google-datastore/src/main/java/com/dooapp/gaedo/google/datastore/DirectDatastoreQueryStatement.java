package com.dooapp.gaedo.google.datastore;

import java.beans.PropertyChangeEvent;

import com.dooapp.gaedo.exceptions.range.BadRangeDefinitionException;
import com.dooapp.gaedo.exceptions.range.BadStartIndexException;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBrowser;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.projection.ProjectionBuilder;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;

public class DirectDatastoreQueryStatement<DataType, InformerType extends Informer<DataType>>
		implements QueryStatement<DataType, DataType, InformerType> {
	/**
	 * Current state
	 */
	private State state = State.INITIAL;

	/**
	 * Query used for this statement
	 */
	private QueryBuilder<InformerType> query;
	/**
	 * Service ultimately used to perform query
	 */
	private final DatastoreFinderService<DataType, InformerType> service;

	/**
	 * google datastore
	 */
	private final DatastoreService datastore;
	/**
	 * Expression deduced by letting {@link #query} matching class Informer
	 */
	private QueryExpression filterExpression;
	/**
	 * Service repository used to find services for other objects
	 */
	private ServiceRepository repository;
	/**
	 * Sorting expression used to define sort criterias
	 */
	private SortingExpression sortingExpression = new SortingExpressionImpl();
	/**
	 * Query id
	 */
	private String id;

	public DirectDatastoreQueryStatement(
			QueryBuilder<InformerType> query,
			DatastoreFinderService<DataType, InformerType> datastoreFinderService,
			DatastoreService datastore, ServiceRepository repository) {
		this.query = query;
		this.service = datastoreFinderService;
		this.datastore = datastore;
		this.repository = repository;
	}

	private DataStoreExecutableQuery prepareQuery() {
		try {
			GAEQueryBuilder builder = new GAEQueryBuilder(service.getKind(),
					service.getIdManager(), repository);
			InformerType informer = service.getInformer();
			filterExpression = query.createMatchingExpression(informer);
			filterExpression.accept(builder);
			return builder.getQuery(datastore, sortingExpression);
		} finally {
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
			return createResultsIterable(prepareQuery().getAll(start, end));
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public Iterable<DataType> getAll() {
		try {
			return createResultsIterable(prepareQuery().getAll());
		} finally {
			setState(State.EXECUTED);
		}
	}

	/**
	 * Creates an iterable of hydrated DataType from the Iterable of GAE
	 * entities
	 *
	 * @param asIterable
	 *            input entity iterable
	 * @return a DataTypeiterable
	 */
	private Iterable<DataType> createResultsIterable(Iterable<Entity> asIterable) {
		return new DataTypeIterable<DataType>(service, asIterable);
	}

	@Override
	public DataType getFirst() {
		try {
			Entity entity = prepareQuery().getEntity();
			if (entity == null)
				throw new NoReturnableEntity(filterExpression);
			return service.getObject(entity);
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public QueryStatement<DataType, DataType, InformerType> sortBy(
			SortingBuilder<InformerType> expression) {
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

	public State getState() {
		return state;
	}

	public void setState(State state) {
		if(this.state!=state) {
			State old = this.state;
			this.state = state;
			repository.getSupport().firePropertyChange(
					new PropertyChangeEvent(this, QueryStatement.STATE_PROPERTY,
							old, state));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public <ProjectedValueType> QueryBrowser<ProjectedValueType> projectOn(ProjectionBuilder<ProjectedValueType, DataType, InformerType> projector) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+QueryStatement.class.getName()+"#projectOn has not yet been implemented AT ALL");
	}
}

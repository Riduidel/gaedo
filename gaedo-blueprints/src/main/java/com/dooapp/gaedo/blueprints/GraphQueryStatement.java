package com.dooapp.gaedo.blueprints;

import java.beans.PropertyChangeEvent;
import java.util.EmptyStackException;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.operations.Loader;
import com.dooapp.gaedo.blueprints.queries.BluePrintsQueryBuilder;
import com.dooapp.gaedo.blueprints.queries.DataTypeIterable;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.blueprints.utils.VertexPathNavigator;
import com.dooapp.gaedo.blueprints.utils.VertexPathNavigator.VertexLocation;
import com.dooapp.gaedo.exceptions.range.BadRangeDefinitionException;
import com.dooapp.gaedo.exceptions.range.BadStartIndexException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBrowser;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitorAdapter;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.finders.projection.NoopProjectionBuilder;
import com.dooapp.gaedo.finders.projection.ProjectionBuilder;
import com.dooapp.gaedo.finders.projection.ValueFetcher;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

public class GraphQueryStatement<
		ValueType,
		DataType,
		InformerType extends Informer<DataType>>
	implements QueryStatement<ValueType, DataType, InformerType> {

	/**
	 * Object allowing execution of a projection from an input vertex to the given result.
	 * @author ndx
	 *
	 */
	public class ProjectionExecutor {
		/**
		 * Fetches values for properties from given vertex
		 * @author ndx
		 *
		 */
		private class GraphValueFetcher implements ValueFetcher {

			private Vertex input;

			public GraphValueFetcher(Vertex input) {
				this.input = input;
			}

			@Override
			public <Type> Type getValue(FieldInformer<Type> propertyDescriptor) {
				VertexPathNavigator navigator = new VertexPathNavigator(service.getStrategy(), service.getDriver(), input);
				VertexLocation destination = navigator.navigateOn(propertyDescriptor.getFieldPath());

				Vertex destinationVertex = destination.vertex();
				try {
					// There may remain one unevaluated property - in which case it's a literal one
					Property destinationProperty = destination.property();
					Loader loader = new Loader();
					if(loader.hasLiteralProperty(destinationProperty, destinationVertex)) {
						return (Type) loader.loadSingleLiteral(getClass().getClassLoader(), destinationProperty, destinationVertex, cache);
					}
				} catch(EmptyStackException e) {

				}
				return (Type) service.loadObject(destinationVertex, cache);
			}

		}

		private ObjectCache cache = createPrepopulatedCache();

		public ValueType get(Vertex input) {
			return projector.project(service.getInformer(), getFetcherFor(input));
		}

		private ValueFetcher getFetcherFor(Vertex input) {
			return new GraphValueFetcher(input);
		}

	}

	protected QueryBuilder<? super InformerType> query;
	protected AbstractBluePrintsBackedFinderService<? extends IndexableGraph, DataType, InformerType> service;
	protected ServiceRepository repository;

	public GraphQueryStatement(QueryBuilder<? super InformerType> query,
					AbstractBluePrintsBackedFinderService<? extends IndexableGraph, DataType, InformerType> service,
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
	/**
	 * Projector used to transform browsed vertices into a meaningful result. Notice default value is always the noop projection builder
	 */
	private ProjectionBuilder<ValueType, DataType, InformerType> projector = new NoopProjectionBuilder();

	/**
	 * Method used to visit query and prepare elements to be used.
	 * This method is not intended for any other external use than testing.
	 * @return
	 */
	public GraphExecutableQuery prepareQuery() {
		try {
			if (QueryLog.logger.isLoggable(QueryLog.QUERY_LOGGING_LEVEL)) {
				QueryLog.logger.log(QueryLog.QUERY_LOGGING_LEVEL, "preparing query "+id);
			}
			BluePrintsQueryBuilder<DataType, InformerType> builder = createQueryBuilder(service);
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

	protected BluePrintsQueryBuilder<DataType, InformerType> createQueryBuilder(AbstractBluePrintsBackedFinderService<? extends IndexableGraph, DataType, InformerType> service) {
		return new BluePrintsQueryBuilder<DataType, InformerType>(service);
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
			Iterable<Vertex> vertices = prepareQuery().get(start, end);
			return createResultsIterable(vertices);
		} finally {
			setState(State.EXECUTED);
		}
	}

	/**
	 * Create result iterable from the vertex iterables.
	 * @param iterable list of vertex to navigate
	 * @return
	 */
	private DataTypeIterable<ValueType> createResultsIterable(Iterable<Vertex> iterable) {
		return new DataTypeIterable<ValueType>(iterable, new ProjectionExecutor());
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
			Iterable<Vertex> vertices = prepareQuery().getAll();
			return createResultsIterable(vertices);
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
			return new ProjectionExecutor().get(result);
		} finally {
			setState(State.EXECUTED);
		}
	}

	@Override
	public QueryStatement<ValueType, DataType, InformerType> sortBy(SortingBuilder<InformerType> expression) {
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

	@Override
	public <ProjectedValueType> QueryBrowser<ProjectedValueType> projectOn(ProjectionBuilder<ProjectedValueType, DataType, InformerType> projector) {
		GraphQueryStatement<ProjectedValueType, DataType, Informer<DataType>> returned = new GraphQueryStatement(query, service, repository);
		returned.projector = (ProjectionBuilder<ProjectedValueType, DataType, Informer<DataType>>) projector;
		return returned;
	}

}

/**
 * 
 */
package com.dooapp.gaedo.finders.root;

import java.beans.PropertyChangeEvent;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;

/**
 * base class for query executable statements. This class simply provides common behaviour, like {@link #buildQueryExpression()} or a default implementation
 * of {@link #sortBy(SortingExpression)} 
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public abstract class AbstractQueryStatement<DataType, InformerType extends Informer<DataType>>
		implements QueryStatement<DataType, InformerType> {
	private State state = State.INITIAL;
	/**
	 * Used query builder
	 */
	protected final QueryBuilder<InformerType> query;
	/**
	 * Class informer used to build query
	 */
	private InformerType informer;
	/**
	 * Used sorting expression
	 */
	private SortingExpression sortingExpression = new SortingExpressionImpl();
	/**
	 * Query expression used to filter content
	 */
	private QueryExpression queryExpression;
	/**
	 * Emitter used to fire events
	 */
	private PropertyChangeEmitter emitter;
	/**
	 * Query id, should be set as soon as possible
	 */
	private String id;

	public AbstractQueryStatement(QueryBuilder<InformerType> query,
			InformerType informer, PropertyChangeEmitter emitter) {
		super();
		this.query = query;
		this.informer = informer;
		this.emitter = emitter;
	}

	/**
	 * Lazy build query expression from informations about service managed class and
	 * input query.
	 * Once built, it is memorized in {@link #queryExpression}. As a consequence, one may not modify the source query expression
	 * 
	 * @return
	 */
	protected QueryExpression buildQueryExpression() {
		if(queryExpression==null) {
			queryExpression = query.createMatchingExpression(informer);
			setState(State.MATCHING);
		}
		return queryExpression;
	}

	@Override
	public QueryStatement<DataType, InformerType> sortBy(SortingBuilder<InformerType> expression) {
		this.sortingExpression = expression.createSortingExpression(informer);
		setState(State.SORTING);
		return this;
	}

	/**
	 * Get used sorting expression
	 * @return {@link #sortingExpression}
	 */
	protected SortingExpression getSortingExpression() {
		return sortingExpression;
	}

	/**
	 * Working only after both have been defined
	 */
	@Override
	public void accept(QueryExpressionContainerVisitor visitor) {
		visitor.startVisit(this);
		buildQueryExpression().accept(visitor);
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
			emitter.firePropertyChange(new PropertyChangeEvent(this, QueryStatement.STATE_PROPERTY, old, state));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
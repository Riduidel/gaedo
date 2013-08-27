package com.dooapp.gaedo.finders;

import com.dooapp.gaedo.finders.projection.ProjectionBuilder;

/**
 * A query statement is an element of query made executable
 * @author ndx
 *
 */
public interface QueryStatement<ValueType, DataType, InformerType extends Informer<DataType>> extends QueryExpressionContainer, QueryBrowser<ValueType> {
	/**
	 * State of the query statement. This state is listenable using a PropertyChangeListener
	 * @author ndx
	 *
	 */
	public static enum State {
		/**
		 * Query statement is in initial state
		 */
		INITIAL,
		/**
		 * Query statement is in this state when a query expression has been constructed
		 */
		MATCHING,
		/**
		 * Query expression is in this state when a sorting expression has been constructed. Notice this state is optionnal
		 */
		SORTING,
		/**
		 * Query expression is in this state when a projection expression has been constructed. Notice this state is optionnal
		 */
		PROJECTING,
		/**
		 * Query statement is in this state once it has been executed (with either getFirst, get, or count)
		 */
		EXECUTED;
	}

	public static final String STATE_PROPERTY = "state";

	/**
	 * updates this query statement and creates one using the given sorting expression
	 * @param expression input sorting expression
	 * @return a query statement using the given sorting expression
	 */
	QueryStatement<ValueType, DataType, InformerType> sortBy(SortingBuilder<InformerType> expression);

	void accept(QueryExpressionContainerVisitor visitor);

	/**
	 * Get current state of this query statement
	 * @return
	 */
	public State getState();

	/**
	 * Provides a String identifying the query. This String can be anything, provided it provides a uniqueness of query : the same query should always have the same id,
	 * regardless of its parameters
	 * @return
	 */
	public String getId();

	/**
	 * Sets query id for easy identifying of queries run
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Project query result on given data type.
	 * Notice it is an exit method, that's to say no further processing is possible after that.
	 * @param projector the projection builder used to build result out of data that would normally be used to return values
	 * @return a query browser in which results are expressed in terms of ProjectValueType class
	 */
	public <ProjectedValueType> QueryBrowser<ProjectedValueType> projectOn(ProjectionBuilder<ProjectedValueType, DataType, InformerType> projector);
}

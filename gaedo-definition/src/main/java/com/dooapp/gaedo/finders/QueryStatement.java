package com.dooapp.gaedo.finders;

import com.dooapp.gaedo.exceptions.BadRangeException;
import com.dooapp.gaedo.patterns.Visitable;
import com.dooapp.gaedo.patterns.Visitor;

/**
 * A query statement is an element of query made executable
 * @author ndx
 *
 */
public interface QueryStatement<DataType, InformerType extends Informer<DataType>> extends QueryExpressionContainer {
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
		 * Query statement is in this state once it has been executed (with either getFirst, get, or count)
		 */
		EXECUTED;
	}

	public static final String STATE_PROPERTY = "state";
	/**
	 * Get first available data
	 * @return
	 */
	DataType getFirst();
	/**
	 * Iterate through all available data
	 * @return
	 */
	Iterable<DataType> getAll();
	/**
	 * Iterate through a subset of all results
	 * @param start start index
	 * @param end end index
	 * @return an iterable over the results
	 * @throws may throws a {@link BadRangeException}
	 */
	Iterable<DataType> get(int start, int end);
	/**
	 * Get data count
	 * @return
	 */
	int count();
	
	/**
	 * updates this query statement and creates one using the given sorting expression
	 * @param expression input sorting expression
	 * @return a query statement using the given sorting expression
	 */
	QueryStatement<DataType, InformerType> sortBy(SortingBuilder<InformerType> expression);
	
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
}

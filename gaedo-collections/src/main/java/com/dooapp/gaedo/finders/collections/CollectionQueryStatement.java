package com.dooapp.gaedo.finders.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.dooapp.gaedo.exceptions.range.BadRangeDefinitionException;
import com.dooapp.gaedo.exceptions.range.BadStartIndexException;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.root.AbstractQueryStatement;
import com.dooapp.gaedo.finders.sort.SortingBackedComparator;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;

/**
 * Build a query statement backed by the collection this service relies upon
 * 
 * @author ndx
 * 
 */
public class CollectionQueryStatement<DataType, InformerType extends Informer<DataType>>
		extends AbstractQueryStatement<DataType, InformerType> {
	/**
	 * Collection used for tests
	 */
	private Iterable<DataType> data;

	public CollectionQueryStatement(QueryBuilder<InformerType> query,
			InformerType informer, Iterable<DataType> data,
			PropertyChangeEmitter emitter) {
		super(query, informer, emitter);
		this.data = data;
	}

	/**
	 * Count the number of elements that matches the QueryExpression
	 */
	public int count() {
		try {
			return selectAll().size();
		} finally {
			setState(State.EXECUTED);
		}
	}

	public Iterable<DataType> get(int start, int end) {
		try {
			if (start < 0) {
				throw new BadStartIndexException(start);
			} else if (end < start) {
				throw new BadRangeDefinitionException(start, end);
			}
			return selectAll().subList(start, end);
		} finally {
			setState(State.EXECUTED);
		}
	}

	public Iterable<DataType> getAll() {
		try {
			Iterable<DataType> returned = selectAll();
			if (getSortingExpression() != null && getSortingExpression().iterator().hasNext()) {
				returned = new TreeSet<DataType>(createComparator());
			}
			return returned;
		} finally {
			setState(State.EXECUTED);
		}
	}

	/**
	 * Creates a comparator from the sorting expression
	 * 
	 * @return
	 */
	private Comparator<? super DataType> createComparator() {
		return new SortingBackedComparator<DataType>(getSortingExpression());
	}

	/**
	 * For getting all data, we build a matcher by calling
	 * {@link #createMatcher()} then use it against all elements of collection
	 */
	private List<DataType> selectAll() {
		List<DataType> returned = new ArrayList<DataType>();
		Matcher<DataType> expression = createMatcher();
		for (DataType element : data) {
			if (expression.matches(element)) {
				returned.add(element);
			}
		}
		return returned;
	}

	/**
	 * Creates a matcher by visiting the QueryExpression
	 * 
	 * @return
	 */
	private Matcher<DataType> createMatcher() {
		QueryExpression expression = buildQueryExpression();
		Matcher<DataType> matcher = new Matcher<DataType>();
		// When the expression accepts the matcher, the matcher will construct
		// its evaluation tree
		expression.accept(matcher);
		return matcher;
	}

	public DataType getFirst() {
		try {
			return selectAll().get(0);
		} finally {
			setState(State.EXECUTED);
		}
	}
}
package com.dooapp.gaedo.finders.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.dooapp.gaedo.exceptions.range.BadRangeDefinitionException;
import com.dooapp.gaedo.exceptions.range.BadStartIndexException;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBrowser;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.projection.ProjectionBuilder;
import com.dooapp.gaedo.finders.root.AbstractQueryStatement;
import com.dooapp.gaedo.finders.sort.SortingBackedComparator;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;

/**
 * Build a query statement backed by the collection this service relies upon
 *
 * @author ndx
 *
 */
public class CollectionQueryStatement<ValueType, DataType, InformerType extends Informer<DataType>>
		extends AbstractQueryStatement<ValueType, DataType, InformerType> {
	/**
	 * Collection used for tests
	 */
	private Iterable<DataType> data;

	public CollectionQueryStatement(QueryBuilder<? super InformerType> query,
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

	public Iterable<ValueType> get(int start, int end) {
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

	public Iterable<ValueType> getAll() {
		try {
			return selectAll();
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
	private List<ValueType> selectAll() {
		Collection<DataType> matching = null;
		if (getSortingExpression() != null && getSortingExpression().iterator().hasNext()) {
			matching = new TreeSet<DataType>(createComparator());
		} else {
			matching = new ArrayList<DataType>();
		}
		Matcher<DataType> expression = createMatcher();
		for (DataType element : data) {
			// select
			if (expression.matches(element)) {
				// add (sorting is done here, but not projection, which is done in second time - yup, it's sub-optimal)
				matching.add(element);
			}
		}
		// Typical use case for guava ? Sure man !
		// but I don't want to rely on that behemoth lib here
		List<ValueType> returned = new ArrayList<ValueType>();
		for(DataType element : matching) {
			if(projector==null) {
				returned.add((ValueType) element);
			} else {
				returned.add(projector.project(informer, new CollectionValueFetcher<DataType>(element)));
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

	public ValueType getFirst() {
		try {
			return selectAll().get(0);
		} finally {
			setState(State.EXECUTED);
		}
	}

	/**
	 * For the sake of simplicity, we simply set value and cast.
	 * This is obviously not optimal, theoretically speaking, but the usage pattern of query builders is to create one for each call
	 * so there is no big deal with that ... excepted if someone, somehow, abuse the system
	 * @param projector
	 * @return
	 * @see com.dooapp.gaedo.finders.QueryStatement#projectOn(com.dooapp.gaedo.finders.projection.ProjectionBuilder)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <ProjectedValueType> QueryBrowser<ProjectedValueType> projectOn(ProjectionBuilder<ProjectedValueType, DataType, InformerType> projector) {
		this.projector = (ProjectionBuilder<ValueType, DataType, Informer<DataType>>) projector;
		setState(State.PROJECTING);
		return (QueryBrowser<ProjectedValueType>) this;
	}
}
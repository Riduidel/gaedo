package com.dooapp.gaedo.finders.dynamic;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.dooapp.gaedo.exceptions.finder.dynamic.MethodBindingException;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;

/**
 * Map of the available query modes (as defined by {@link QueryStatement})
 * @author Nicolas
 *
 */
public enum Mode {
	COUNT("countBy", 0),
	FIND_ALL("findAllBy", 0),
	FIND_RANGE("findRangeBy", 2),
	FIND_ONE("findOneWith", 0);
	
	/**
	 * Prefix used for method name
	 */
	private final String prefix;
	
	/**
	 * Offset for method parameters
	 */
	private final int offset; 

	private Mode(String prefix, int offset) {
		this.prefix = prefix;
		this.offset = offset;
	}

	/**
	 * Builds a finder from the service and invoke the correct method upon it.
	 * @param backEnd backend on which query is performed
	 * @param expression query expression used for lookup
	 * @param parameters remaining parameters of input method, used by some modes
	 * @param sortingBuilder used sorting builder
	 * @param id an id that will be given to query
	 * @return the result of this mode query statement execution
	 */
	public Object execute(FinderCrudService backEnd, final QueryExpression expression, final SortingExpression sortingExpression, Object[] parameters, String id) {
		QueryStatement queryStatement = buildQueryStatement(backEnd,
				expression, sortingExpression);
		return execute(queryStatement, parameters, id);
	}

	/**
	 * Execute given finder in the correct mode
	 * @param backEnd backend on which query is performed
	 * @param parameters remaining parameters of input method, used by some modes
	 * @param id an id that will be given to query
	 * @return the result of this mode query statement execution
	 */
	public Object execute(QueryStatement queryStatement, Object[] parameters, String id) {
		queryStatement.setId(id);
		switch(this) {
		case COUNT:
			return queryStatement.count();
		case FIND_ALL:
			return queryStatement.getAll();
		case FIND_ONE:
			return queryStatement.getFirst();
		case FIND_RANGE:
			return queryStatement.get((Integer) parameters[0], (Integer) parameters[1]);
		}
		throw new UnsupportedOperationException("mode entered is not one of Mode values ??? How is it possible ? please fill a bug report at gaedo-definition");
	}

	/**
	 * Build query statement from required parameters
	 * @param backEnd backend used to run the qery
	 * @param expression query expression
	 * @param sortingExpression sorting expression
	 * @return the {@link QueryStatement} used to execute the query
	 */
	@SuppressWarnings("unchecked")
	private QueryStatement buildQueryStatement(
			FinderCrudService backEnd, final QueryExpression expression,
			final SortingExpression sortingExpression) {
		QueryStatement queryStatement = backEnd.find().matching(new QueryBuilder<Informer<?>>() {

			@Override
			public QueryExpression createMatchingExpression(
					Informer<?> object) {
				return expression;
			}
		}).sortBy(new SortingBuilder<Informer<?>>() {

			@Override
			public SortingExpression createSortingExpression(
					Informer<?> informer) {
				return sortingExpression;
			}
		});
		return queryStatement;
	}

	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public String toString() {
		return getPrefix();
	}

	/**
	 * Get an offset for parameters array, used to put mode specific parameters in method parameters.
	 * @return 0 in all cases excepted {@link #FIND_RANGE}
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Check correctness of mode specific parameters
	 * @param method checked method
	 * @param modeArgs mode specific arguments (this array must have the length defined by {@link #getOffset()}
	 * @param resolver associated method resolver
	 * @throws MethodBindingException if anything fails
	 */
	public void checkParametersClasses(Method method, Type[] modeArgs, DynamicFinderMethodResolver resolver) {
		switch(this) {
		case COUNT:
			return;
		case FIND_ALL:
			return;
		case FIND_ONE:
			return;
		case FIND_RANGE:
			for(Type t : modeArgs) {
				if(!(Integer.class.equals(t) || Integer.TYPE.equals(t))) {
					throw new MethodBindingException(method, resolver, Arrays.asList(prefix+" methods require the first two parameters to be int or integer, which is not the case here"));
				}
			}
			return;
		}
		throw new UnsupportedOperationException("mode entered is not one of Mode values ??? How is it possible ? please fill a bug report at gaedo-definition");
	}
}
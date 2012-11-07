/**
 * 
 */
package com.dooapp.gaedo.finders.dynamic;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dooapp.gaedo.exceptions.finder.dynamic.MethodBindingException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildQueryExpressionException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryExpressionContainer;
import com.dooapp.gaedo.finders.QueryExpressionContainerVisitor;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.dooapp.gaedo.patterns.Visitable;
import com.dooapp.gaedo.utils.MethodResolver;
import com.dooapp.gaedo.utils.Utils;

/**
 * Resolves a method call to a an executable set of method calls on back end servcie
 * @author ndx
 *
 */
public class DynamicFinderMethodResolver implements QueryExpressionContainer, MethodResolver {
	/**
	 * Combination mode for method
	 */
	private Combinator combinator = Combinator.And;
	/**
	 * We use a collection of entries to associate the {@link FieldInformer} in a unique way to the {@link Method} used to get associated expression
	 */
	private Collection<Entry<FieldInformer, Method>> queryExpressions = new LinkedList<Entry<FieldInformer, Method>>();
	/**
	 * Mode defining how the produced QueryStatement will be executed
	 */
	private Mode mode;
	
	/**
	 * Back-end on which all invocations are done
	 */
	private FinderCrudService backEnd;
	
	/**
	 * Execution id for this method resolution, built from some method text
	 */
	private final String executionId;
	
	/**
	 * Method return type, used to provide some type coercion
	 */
	private Class<?> returnType;
	
	/**
	 * used sorting expression
	 */
	private SortingExpression sortingExpression = new SortingExpressionImpl();

	public DynamicFinderMethodResolver(
			FinderCrudService backEnd,
			Method method) {
		this(backEnd, method.toGenericString(), method.getReturnType());
	}

	public DynamicFinderMethodResolver(
			FinderCrudService backEnd,
			String executionId,
			Class<?> returnType) {
		this.backEnd = backEnd;
		this.executionId = executionId;
		this.returnType = returnType;
	}

	/**
	 * Adds one expression to the list of checked query expressions
	 * @param informer
	 * @param method
	 */
	public void addQueryExpression(FieldInformer informer, Method method) {
		queryExpressions.add(new com.dooapp.gaedo.utils.Entry<FieldInformer, Method>(informer, method));
	}

	/**
	 * Adds one expression to the list of checked query expressions
	 * @param informer
	 * @param method
	 */
	public void addSortingExpression(FieldInformer informer, SortingExpression.Direction direction) {
		sortingExpression.add(informer, direction);
	}

	/**
	 * Build the QueryExpression, then gently ask {@link Mode#execute(FinderCrudService, QueryExpression, SortingBuilder, Object[])} to perform query.
	 * Finally, for findAll, we provide the additionnal feature to put obejcts in the correct Iterable sub-interface associated type
	 * @param args
	 * @return
	 */
	public Object call(Object[] args) {
		Collection<QueryExpression> resolvedExpressions = new LinkedList<QueryExpression>();
		int offset = mode.getOffset();
		for(Map.Entry<FieldInformer, Method> entry : queryExpressions) {
			int consumableLength = entry.getValue().getParameterTypes().length;
			Object[] methodArgs = new Object[consumableLength];
			System.arraycopy(args, offset, methodArgs, 0, consumableLength);
			offset += consumableLength;
			try {
				resolvedExpressions.add((QueryExpression) entry.getValue().invoke(entry.getKey(), methodArgs));
			} catch (Exception e) {
				throw new UnableToBuildQueryExpressionException(entry.getKey(), entry.getValue(), methodArgs, e);
			}
		}
		QueryExpression queryExpression = combinator.create(resolvedExpressions);
		// Creates an array of parameters going from 0 to offset
		Object[] modeArgs = new Object[mode.getOffset()];
		System.arraycopy(args, 0, modeArgs, 0, mode.getOffset());
		Object returned = mode.execute(backEnd, queryExpression, sortingExpression, modeArgs, executionId);
		// Find all tricky trick
		if(mode==Mode.FIND_ALL || mode==Mode.FIND_RANGE) {
			// If method return type is not iterable but a sub-interface, put my merlin's cape !
			if(!Iterable.class.equals(returnType)) {
				Collection toReturn = Utils.generateCollection(returnType, null);
				Iterable iterable = (Iterable) returned;
				for(Object data : iterable) {
					toReturn.add(data);
				}
				returned = toReturn;
			}
		}
		return returned;
	}

	/**
	 * Check that this MethodResolver uses the same parameters stack than the given Method object
	 * @param method input method
	 */
	public void checkParametersClasses(Method method) {
		Collection<String> errors = new LinkedList<String>();
		Type[] consumableParameters = method.getGenericParameterTypes();
		List<Type> consumableList = new LinkedList<Type>(Arrays.asList(consumableParameters));
		// Creates an array of parameters going from 0 to offset
		Type[] modeArgs = new Type[mode.getOffset()];
		System.arraycopy(consumableParameters, 0, modeArgs, 0, mode.getOffset());
		mode.checkParametersClasses(method, modeArgs, this);
		int paramIndex = mode.getOffset();
		for(Map.Entry<FieldInformer, Method> entry : queryExpressions) {
			Class<?>[] parameters = entry.getValue().getParameterTypes();
			for(Class<?> p : parameters) {
				if(consumableList.size()==0) {
					errors.add("there are not enough parameters to match method call "+entry.getKey().toString()+" "+entry.getValue().toGenericString());
					continue;
				}
				Type toCompare = consumableList.remove(0);
				if(toCompare instanceof Class<?>) {
					Class<?> toCompareClass = (Class<?>) toCompare;
					if(toCompareClass.isPrimitive()) {
						// Do some magick to retrieve associated class
						toCompareClass = Utils.objectify(toCompareClass);
					}
					if(!p.isAssignableFrom(toCompareClass)) {
						errors.add("parameter "+p.getName()+" of method "+entry.getValue().toGenericString()+"cannot use value of type "+toCompareClass.getName());
					}
				} else {
					throw new UnsupportedOperationException(toCompare.getClass().getName()+" not supported in that case. please fill a gaedo-definition bug report");
				}
			}
			paramIndex++;
		}
		if(errors.size()>0) {
			throw new MethodBindingException(method, this, errors);
		}
	}

	public Mode getMode() {
		return mode;
	}

	public void setCombinator(Combinator c) {
		combinator = c;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	@Override
	public String toString() {
		StringBuilder sOut = new StringBuilder();
		sOut.append("combination : ").append(combinator).append("\n");
		sOut.append("expression :\n");
		for(Map.Entry<FieldInformer, Method> entry : queryExpressions) {
			sOut.append("\t").append(entry.getKey().toString()).append("\tinvoking\t").append(entry.getValue().toGenericString()).append("\n");
		}
		return sOut.toString();
	}

	/**
	 * Check that both parameters and return types are compliant with what we expect
	 * @param method
	 */
	public void checkMethod(Method method) {
		checkParametersClasses(method);
		checkReturnType(method);
	}

	/**
	 * Check that implemented method return type is compliant with internal code.
	 * Notice we can perform some type escalation (from Iterable to Collection, as an example)
	 * @param method
	 */
	private void checkReturnType(Method method) {
		Class<?> returnType = method.getReturnType();
		switch(mode) {
		case COUNT:
			if(!(Integer.class.isAssignableFrom(returnType) || Integer.TYPE.isAssignableFrom(returnType) ||
					Long.class.isAssignableFrom(returnType) || Long.TYPE.isAssignableFrom(returnType))) {
				throw new BadReturnTypeException(method, returnType, Integer.class, int.class, long.class, Long.class);
			}
			break;
		case FIND_ALL:
			if(!Iterable.class.isAssignableFrom(returnType)) {
				throw new BadReturnTypeException(method, returnType, Iterable.class);
			}
			break;
		case FIND_ONE:
			if(!returnType.isAssignableFrom(backEnd.getContainedClass())) {
				throw new BadReturnTypeException(method, returnType, backEnd.getContainedClass());
			}
		}
	}

	@Override
	public void accept(QueryExpressionContainerVisitor visitor) {
		visitor.startVisit(this);
		// TODO find a way to visit non-constructed query expressions !
		sortingExpression.accept(visitor);
		visitor.endVisit(this);
	}
}
package com.dooapp.gaedo.finders.dynamic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingQueryExpressionException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingFieldException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingModeException;
import com.dooapp.gaedo.exceptions.finder.dynamic.UnableToBuildDueToMissingSortingExpressionException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.PropertyProviderUtils;
import com.dooapp.gaedo.utils.BasicInvocationHandler;
import com.dooapp.gaedo.utils.CallMethodOnObjectResolver;
import com.dooapp.gaedo.utils.MethodResolver;
import com.dooapp.gaedo.utils.Utils;
import com.dooapp.gaedo.utils.VirtualMethodCreationException;

/**
 * A dynamic finder handler is the class that will transform methods declared in provided interface into calls to back end.
 * 
 * @author ndx
 *
 * @param <DataType>
 */
public class DynamicFinderHandler<DataType> extends BasicInvocationHandler<DataType, MethodResolver> implements InvocationHandler {
	/**
	 * text used to combine one or more element of sort
	 */
	private static final String SORT_BY_COMBINATOR = "Then";

	/**
	 * Prefix used for toggling mode from filter to sort
	 */
	private static final String SORT_BY_PREFIX = "SortBy";

	/**
	 * Enum used when creating a method resolver to select in which mode we currently are.
	 * This mode will determine how consumed text of method signature is transformed into executable MethodResolver code
	 * @author ndx
	 *
	 */
	private enum ParametersConstructionMode {
		FILTER,
		SORT;

		public String consumeText(DynamicFinderHandler handler, String methodText, String consumableText, Map<String, String> fieldNames,DynamicFinderMethodResolver created) {
			switch(this) {
			case FILTER:
				return handler.consumeFilter(methodText, consumableText, fieldNames, created);
			case SORT:
				return handler.consumeSort(methodText, consumableText, fieldNames, created);
			}
			throw new UnsupportedOperationException("are you kiddin or what ? You used an undeclared mode named "+name()+"\nplease fill a bug report at gaedo-definition");
		}
	}
	private static final Logger logger  = Logger.getLogger(DynamicFinderHandler.class.getName());
	
	/**
	 * Backend which will receive forwarded calls
	 */
	protected final FinderCrudService<DataType, ? extends Informer<DataType>> backEnd;  
	
	/**
	 * Used property provider
	 */
	protected final PropertyProvider propertyProvider;

	/**
	 * Define the handler by giving it the class to implement and the back end used to implement it.
	 * Due to issue #18, we eager create method resolvers.
	 * @param toImplement interface to implement
	 * @param backEnd back end that will receive calls
	 */
	public DynamicFinderHandler(Class<DataType> toImplement,
			FinderCrudService<DataType, ?> backEnd, PropertyProvider provider) {
		super(toImplement);
		this.backEnd = backEnd;
		this.propertyProvider = provider;
		createAllMethodResolvers(toImplement);
	}

	/**
	 * Consume text to populate the sorting expression
	 * @category dynamic_method_builder
	 */
	public String consumeSort(String methodString, String consumableText, Map<String, String> fieldNames,DynamicFinderMethodResolver created) {
		boolean found = false;
		for(Map.Entry<String, String> fieldEntry: fieldNames.entrySet()) {
			if(consumableText.startsWith(fieldEntry.getKey())) {
				consumableText = consumableText.substring(fieldEntry.getKey().length());
				FieldInformer informer = backEnd.getInformer().get(fieldEntry.getValue());
				found = false;
				for(SortingExpression.Direction direction : SortingExpression.Direction.values()) {
					if(consumableText.startsWith(direction.getText())) {
						consumableText = consumableText.substring(direction.getText().length());
						created.addSortingExpression(informer, direction);
						found = true;
					}
				}
				if(!found) {
					throw new UnableToBuildDueToMissingSortingExpressionException(consumableText, methodString, fieldEntry.getKey(),SortingExpression.Direction.values());
				}
				// Sorting can only be combined with And text
				if(consumableText.startsWith(SORT_BY_COMBINATOR)) {
					consumableText = consumableText.substring(SORT_BY_COMBINATOR.length());
				}
				found = true;
			}
		}
		return consumableText;
	}

	/**
	 * Consume text to populate the query expression
	 * @category dynamic_method_builder
	 */
	public String consumeFilter(String methodString, String consumableText, Map<String, String> fieldNames,DynamicFinderMethodResolver created) {
		boolean found = false;
		for(Map.Entry<String, String> fieldEntry: fieldNames.entrySet()) {
			if(consumableText.startsWith(fieldEntry.getKey())) {
				consumableText = consumableText.substring(fieldEntry.getKey().length());
				FieldInformer informer = backEnd.getInformer().get(fieldEntry.getValue());
				Map<String, Method> queries = Utils.getUppercasedMap(informer.getClass().getMethods());
				found = false;
				for(Map.Entry<String, Method> methodEntry : queries.entrySet()) {
					if(consumableText.startsWith(methodEntry.getKey())) {
						consumableText = consumableText.substring(methodEntry.getKey().length());
						created.addQueryExpression(informer, methodEntry.getValue());
						found = true;
					}
				}
				// TODO insert here code for call chaining
				if(!found) {
					throw new UnableToBuildDueToMissingQueryExpressionException(consumableText, methodString, fieldEntry.getKey(), queries.keySet());
				}
				for(Combinator c: Combinator.values()) {
					if(consumableText.startsWith(c.getText())) {
						consumableText = consumableText.substring(c.getText().length());
						created.setCombinator(c);
					}
				}
				found = true;
			}
		}
		return consumableText;
	}

	/**
	 * Create the resolver for the method name by consuming its string declaration
	 * @param method method to map
	 * @return a method resolver for the given method name
	 * @category dynamic_method_builder
	 */
	public MethodResolver createResolver(Method method) throws VirtualMethodCreationException {
		Class<?> declaringClass = method.getDeclaringClass();
		if(declaringClass.isAssignableFrom(DynamicFinder.class)) {
			return new CallMethodOnObjectResolver(backEnd, method);
		} else if(declaringClass.isAssignableFrom(DynamicFinderHandler.class)) {
			return new CallMethodOnObjectResolver(this, method);
		} else if(declaringClass.isAssignableFrom(IdBasedService.class)) {
			return new CallMethodOnObjectResolver(backEnd, method);
		} else {
			return createResolverForDynamicMethod(method);
		}
	}

	/**
	 * When using dynamic languages, we can't access to high level type information like in Java, but rather to a name and method parameters (which are actual ones).
	 * As a consequence, a weaker type handling is to use
	 * @param method
	 * @return
	 */
	private DynamicFinderMethodResolver createResolverForDynamicMethod(String methodName, Object[] methodArgs) {
		boolean found = false;
		String consumableText = methodName;
		
		ParametersConstructionMode mode = ParametersConstructionMode.FILTER;
		DynamicFinderMethodResolver created = new DynamicFinderMethodResolver(backEnd, methodName, Object.class);
		for(Mode m : Mode.values()) {
			if(consumableText.startsWith(m.getPrefix())) {
				created.setMode(m);
				found = true;
			}
		}
		if(!found) {
			throw new UnableToBuildDueToMissingModeException(methodName);
		}
		consumableText = consumableText.substring(created.getMode().getPrefix().length());
		Map<String, String> fieldNames = Utils.getUppercasedMap(PropertyProviderUtils.getAllProperties(propertyProvider, backEnd.getContainedClass()));
		while(consumableText.length()>0) {
			String initialText = consumableText;
			consumableText = mode.consumeText(this, methodName, consumableText, fieldNames, created);
			if(consumableText.startsWith(SORT_BY_PREFIX)) {
				if(mode==ParametersConstructionMode.FILTER) {
					mode = ParametersConstructionMode.SORT;
					consumableText = consumableText.substring(SORT_BY_PREFIX.length());
				}
			}
			if(initialText.equals(consumableText)) {
				throw new UnableToBuildDueToMissingFieldException(consumableText, methodName, fieldNames.keySet());
			}
		}
		return created;
	}

	/**
	 * Create resolver for method object
	 * @param method
	 * @return
	 */
	private DynamicFinderMethodResolver createResolverForDynamicMethod(
			Method method) {
		boolean found = false;
		String consumableText = method.getName();
		ParametersConstructionMode mode = ParametersConstructionMode.FILTER;
		DynamicFinderMethodResolver created = new DynamicFinderMethodResolver(backEnd, method);
		for(Mode m : Mode.values()) {
			if(consumableText.startsWith(m.getPrefix())) {
				created.setMode(m);
				found = true;
			}
		}
		if(!found) {
			throw new UnableToBuildDueToMissingModeException(method);
		}
		consumableText = consumableText.substring(created.getMode().getPrefix().length());
		Map<String, String> fieldNames = Utils.getUppercasedMap(PropertyProviderUtils.getAllProperties(propertyProvider, backEnd.getContainedClass()));
		while(consumableText.length()>0) {
			String initialText = consumableText;
			consumableText = mode.consumeText(this, method.toGenericString(), consumableText, fieldNames, created);
			if(consumableText.startsWith(SORT_BY_PREFIX)) {
				if(mode==ParametersConstructionMode.FILTER) {
					mode = ParametersConstructionMode.SORT;
					consumableText = consumableText.substring(SORT_BY_PREFIX.length());
				}
			}
			if(initialText.equals(consumableText)) {
				throw new UnableToBuildDueToMissingFieldException(consumableText, method, fieldNames.keySet());
			}
		}
		// Now created has been terminated, check method corresponds to what we expect
		created.checkMethod(method);
		return created;
	}

	public FinderCrudService<DataType, ? extends Informer<DataType>> getBackEnd() {
		return backEnd;
	}

	public Class<DataType> getToImplement() {
		return toImplement;
	}
}

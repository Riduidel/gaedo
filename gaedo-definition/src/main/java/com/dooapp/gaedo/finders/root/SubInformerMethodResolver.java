package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.utils.MethodResolver;

/**
 * Sub-informer class method resolver
 * @author ndx
 *
 */
public class SubInformerMethodResolver implements MethodResolver {

	private Class<?> returnType;
	private Informer<?> informer;
	private String fieldName;

	public SubInformerMethodResolver(Class<?> returnType,
			Informer<?> realInformer, String realFieldName) {
		this.returnType = returnType;
		this.informer = realInformer;
		this.fieldName = realFieldName;
	}

	/**
	 * Create a MethodResolver for the expected FieldInformer result and the given real informer
	 * @param returned expected return value, that won't be kept, as a weird kind of optimization
	 * @param realInformer real informer object
	 */
	public SubInformerMethodResolver(FieldInformer returned,
			Informer<?> realInformer) {
		this(returned.getClass(), realInformer, returned.getField().getName());
	}

	/**
	 * Call method and return given result
	 * @param invokedArgs
	 * @return
	 */
	@Override
	public Object call(Object[] invokedArgs) {
		return returnType.cast(informer.get(fieldName));
	}

}

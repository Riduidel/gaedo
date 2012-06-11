package com.dooapp.gaedo.finders.collections;

import java.lang.reflect.Field;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.properties.Property;

/**
 * Root class for simple evaluators
 * @author ndx
 *
 * @param <DataType>
 */
public abstract class AbstractBasicEvaluator<DataType> implements Evaluator<DataType> {
	static class NonExistentFieldException extends CrudServiceException {
		public NonExistentFieldException(Property field, Object element) {
			super("The field \"" +
					field.toGenericString()+
					"\"does not exists for class \""+element.getClass().getName()+"\"");
		}
	}

	/**
	 * Field to introspect
	 */
	private final Property source;

	public AbstractBasicEvaluator(Property source) {
		super();
		this.source = source;
	}

	/**
	 * Common implementation of reflection-backed value getter
	 * @param element
	 * @return
	 */
	protected Object getValue(DataType element) {
		try {
			if(source==null)
				return element;
			else
				return source.get(element);
		} catch (Exception e) {
			throw new NonExistentFieldException(source, element);
		}
	}

	/**
	 * Basic evaluators never support adding subevaluators; As a consequence, they always fire an {@link UnsupportedOperationException}
	 */
	public final void add(Evaluator<DataType> subEvaluator) {
		throw new UnsupportedOperationException("don't thing about adding a child evaluator to a basic condition "+getClass().getSimpleName()+" man !");
	}
}

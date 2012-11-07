package com.dooapp.gaedo.finders.root;

import java.util.HashMap;
import java.util.Map;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Informer lcoator allowing ordered delegation between a first locator ... and a second (crazy, no ?).
 * This locator is not intended for general use, but rather for behaviour extension in particular cases.
 * @author ndx
 *
 */
public class DelegatingInformerLocator implements FieldInformerLocator {

	private FieldInformerLocator first;
	private FieldInformerLocator second;

	public DelegatingInformerLocator(FieldInformerLocator firstLocator, FieldInformerLocator secondLocator) {
		this.first = firstLocator;
		this.second = secondLocator;
	}
	
	private Map<FieldInformerLocator, Exception> map(CrudServiceException e1, CrudServiceException e2) {
		Map<FieldInformerLocator, Exception> returned = new HashMap<FieldInformerLocator, Exception>();
		returned.put(first, e1);
		returned.put(second, e2);
		return returned;
	}

	@Override
	public FieldInformer getInformerFor(Property field) {
		try {
			return first.getInformerFor(field);
		} catch(CrudServiceException e1) {
			try {
				return second.getInformerFor(field);
			} catch(CrudServiceException e2) {
				throw new NoLocatorAllowsFieldException(field, map(e1, e2));
			}
		}
	}

	@Override
	public FieldInformer getInformerFor(Class informedClass, String fieldName) {
		try {
			return first.getInformerFor(informedClass, fieldName);
		} catch(CrudServiceException e1) {
			try {
				return second.getInformerFor(informedClass, fieldName);
			} catch(CrudServiceException e2) {
				throw new NoLocatorAllowsFieldException(informedClass, fieldName, map(e1, e2));
			}
		}
	}

	/**
	 * @return the first
	 * @category getter
	 * @category first
	 */
	public FieldInformerLocator getFirst() {
		return first;
	}

	/**
	 * @param first the first to set
	 * @category setter
	 * @category first
	 */
	public void setFirst(FieldInformerLocator first) {
		this.first = first;
	}

	/**
	 * @return the second
	 * @category getter
	 * @category second
	 */
	public FieldInformerLocator getSecond() {
		return second;
	}

	/**
	 * @param second the second to set
	 * @category setter
	 * @category second
	 */
	public void setSecond(FieldInformerLocator second) {
		this.second = second;
	}
}

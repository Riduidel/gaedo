package com.dooapp.gaedo.finders.root;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.properties.Property;

/**
 * Cumulative field informer locator allows usage of multiple {@link FieldInformerLocator} simultaneoulsy.
 * @author ndx
 *
 */
public class CumulativeFieldInformerLocator implements FieldInformerLocator {
	/**
	 * Collection of known fields locators, used to load entries
	 */
	private Collection<FieldInformerLocator> locators = new LinkedList<FieldInformerLocator>();

	public boolean add(FieldInformerLocator e) {
		return locators.add(e);
	}

	@Override
	public FieldInformer getInformerFor(Property field) {
		FieldInformer returned = null;
		Map<FieldInformerLocator, Exception> thrownDuringSearch = new LinkedHashMap<FieldInformerLocator, Exception>();
		for (FieldInformerLocator locator : locators) {
			try {
				returned = locator.getInformerFor(field);
				if (returned != null) {
					return returned;
				}
			} catch (Exception e) {
				thrownDuringSearch.put(locator, e);
			}
		}
		throw new NoLocatorAllowsFieldException(field, thrownDuringSearch);
	}

	@Override
	public FieldInformer getInformerFor(Class informedClass, String fieldName) {
		FieldInformer returned = null;
		Map<FieldInformerLocator, Exception> thrownDuringSearch = new LinkedHashMap<FieldInformerLocator, Exception>();
		for (FieldInformerLocator locator : locators) {
			try {
				returned = locator.getInformerFor(informedClass, fieldName);
				if (returned != null) {
					return returned;
				}
			} catch (Exception e) {
				thrownDuringSearch.put(locator, e);
			}
		}
		throw new NoLocatorAllowsFieldException(informedClass, fieldName, thrownDuringSearch);
	}

}

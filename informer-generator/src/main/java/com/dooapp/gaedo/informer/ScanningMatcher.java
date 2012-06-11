package com.dooapp.gaedo.informer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public abstract class ScanningMatcher extends ImportResolver<ScanningMatcher> {

	/**
	 * Collection of required interfaces
	 */
	protected Collection<String> requiredElements = new LinkedList<String>();
	/**
	 * Map linking simple interface name to its package (for easier lookup in {@link #importStrings})
	 */
	protected Map<String, String> simpleElementsNames;

	public ScanningMatcher(String[] elements) {
		this.requiredElements.addAll(Arrays.asList(elements));
	}

	public boolean matches(CompilationUnit searched) {
		if(requiredElements.size()>0) {
			simpleElementsNames = createRequiredInterfacesNames(requiredElements);
			visit(searched, this);
			return requiredElements.size()==0;
		} else {
			return true;
		}
	}

	/**
	 * Map linking simple interface name to its package name
	 * @param requiredInterfaces2 
	 * @return
	 */
	private Map<String, String> createRequiredInterfacesNames(Collection<String> qualifiedNames) {
		Map<String, String> returned = new TreeMap<String, String>();
		for(String name : qualifiedNames) {
			int lastPoint = name.lastIndexOf(".");
			if(lastPoint>=0) {
				returned.put(name.substring(lastPoint+1), name.substring(0, lastPoint));
			} else {
				throw new UnsupportedOperationException("elements of default package are not supported here ... and you're trying to detect presence of \""+name+"\"");
			}
		}
		return returned;
	}

	/**
	 * Test given element to see how well it matches
	 * @param name
	 * @param current
	 */
	protected void testElement(String name, ScanningMatcher current) {
		// it's a qualified name (uncommon, but nevertheless possible
		if(name.indexOf(".")>=0) {
			// Directly check if it can be a part of requiredinterfaces
			if(current.requiredElements.contains(name)) {
				current.requiredElements.remove(name);
			}
		} else {
			// Check if name is a suffix in one of requiredInterfaces
			if(current.simpleElementsNames.containsKey(name)) {
				String packageName = current.simpleElementsNames.get(name);
				String qualifiedName = packageName+"."+name;
				if(importStrings.contains(packageName) || current.importStrings.contains(qualifiedName)) {
					current.requiredElements.remove(qualifiedName);
				}
			}
		}
	}

}

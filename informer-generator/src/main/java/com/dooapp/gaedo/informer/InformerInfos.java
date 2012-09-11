package com.dooapp.gaedo.informer;

import japa.parser.ast.ImportDeclaration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class InformerInfos {
	/**
	 * Declared class package
	 */
	public String classPackage;
	/**
	 * Declared class name
	 */
	public String className;
	/**
	 * Declared superclass simple name. its package will be copied from source class packages imports (ugly, no ?)
	 */
	public String superClassName;
	
	public List<ImportDeclaration> imports = new LinkedList<ImportDeclaration>();
	/**
	 * Map linking properties names to their extracted associated instances
	 */
	public Collection<PropertyInfos> properties;
	
	public String getInformerName() {
		return buildInformerName(className);
	}

	public static String buildInformerName(String className) {
		return className + "Informer";
	}
	
	/**
	 * get extracted property infos for the given property name
	 * @param propertyName
	 * @return null if none found
	 */
	public PropertyInfos getInfosFor(String propertyName) {
		for(PropertyInfos p : properties) {
			if(p.name.equals(propertyName))
				return p;
		}
		return null;
	}

	public String getQualifiedClassName() {
		return classPackage+"."+className;
	}

	public String getQualifiedInformerName() {
		return classPackage+"."+getInformerName();
	}

	/**
	 * Create an abstract informer interface containing only method declarations
	 * @return
	 */
	public String getAbstractInformerName() {
		return buildAbstractInformerName(className);
	}

	public static String buildAbstractInformerName(String className) {
		return "Abstract"+buildInformerName(className);
	}
}

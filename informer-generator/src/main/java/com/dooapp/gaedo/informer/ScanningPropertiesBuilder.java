package com.dooapp.gaedo.informer;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ScanningPropertiesBuilder extends VoidVisitorAdapter<InformerInfos> {
	private boolean includeStaticProperties;
	/**
	 * Elements from this collection can be in two forms
	 * <ul>
	 * <li>Simple strings</li>
	 * <li>Javadoc-comaptible properties : package.class#property</li>
	 * </ul>
	 */
	private Collection<String> excludedProperties;

	public ScanningPropertiesBuilder(boolean includeStaticProperties, Collection<String> excludedProperties) {
		this.includeStaticProperties = includeStaticProperties;
		this.excludedProperties = excludedProperties;
	}

	@Override
	public void visit(ImportDeclaration n, InformerInfos arg) {
		arg.imports.add(n);
	}
	
	@Override
	public void visit(PackageDeclaration n, InformerInfos arg) {
		arg.classPackage = n.getName().toString();
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration classDeclaration, InformerInfos informer) {
		if (!classDeclaration.isInterface()) {
			informer.className = classDeclaration.getName();
			if (classDeclaration.getExtends()!=null && classDeclaration.getExtends().size() > 0) {
				informer.superClassName = classDeclaration.getExtends().get(0).getName();
			}
			informer.properties = filter(createPropertiesFrom(classDeclaration.getMembers()), informer);
		}
	}

	/**
	 * Filter map of properties using excluded properties
	 * @param createPropertiesFrom
	 * @param informer TODO
	 * @return
	 */
	private Collection<PropertyInfos> filter(Map<String, PropertyInfos> createPropertiesFrom, InformerInfos informer) {
		Collection<String> removed = new LinkedList<String>(); 
		String qualifiedClassName = informer.classPackage+"."+informer.className;
		// Now exclude properties based upon their name and their matching
		for(String s : excludedProperties) {
			int dashIndex = s.indexOf('#');
			if(dashIndex>0) {
				if(s.substring(0, dashIndex).equals(qualifiedClassName)) {
					removed.add(s.substring(dashIndex+1));
				}
			} else {
				// a trick : if map contains property name, it will be removed. Otherwise nothing will happen
				removed.add(s);
			}
		}
		for(String s : removed) {
			createPropertiesFrom.remove(s);
		}
		return createPropertiesFrom.values();
	}

	/**
	 * Create a list of properties from a class body declaration
	 * @param members list of class members (fields and methods)
	 * @return a map linking property name to its value
	 */
	private Map<String, PropertyInfos> createPropertiesFrom(List<BodyDeclaration> members) {
		Map<String, PropertyInfos> properties = new TreeMap<String, PropertyInfos>();
		for (BodyDeclaration b : members) {
			if (b instanceof FieldDeclaration) {
				FieldDeclaration f = (FieldDeclaration) b;
				if(includeStaticProperties || !Modifier.isStatic(f.getModifiers())) {
					for (VariableDeclarator d : f.getVariables()) {
						Type type = f.getType();
						String name = d.getId().getName();
						addInfosFor(properties, InfosTypes.FIELD, name, type);
					}
				}
			} else if (b instanceof MethodDeclaration) {
				MethodDeclaration m = (MethodDeclaration) b;
				if(includeStaticProperties || !Modifier.isStatic(m.getModifiers())) {
					if (isGetter(m)) {
						addInfosFor(properties, InfosTypes.GETTER, m.getName(), m.getType());
					} else if (isSetter(m)) {
						addInfosFor(properties, InfosTypes.SETTER, m.getName(), m.getParameters().get(0).getType());
					}
				}
			}
		}
		return properties;
	}

	private void addInfosFor(Map<String, PropertyInfos> properties, InfosTypes field, String name, Type type) {
		String propertyName = field.getNameFor(name);
		if (!properties.containsKey(propertyName)) {
			properties.put(propertyName, new PropertyInfos());
		}
		PropertyInfos propertyInfos = properties.get(propertyName);
		propertyInfos.name = propertyName;
		propertyInfos.type = type;
		switch (field) {
		case FIELD:
			propertyInfos.field = name;
			break;
		case GETTER:
			propertyInfos.getter = name;
			break;
		case SETTER:
			propertyInfos.setter = name;
			break;
		}
	}

	private boolean isSetter(MethodDeclaration m) {
		if (m.getName().startsWith(InfosTypes.SETTER.prefix)) {
			return m.getType().toString().equals("void");
		}
		return false;
	}

	private boolean isGetter(MethodDeclaration m) {
		if (m.getName().startsWith(InfosTypes.GETTER.prefix)) {
			return m.getParameters()==null || m.getParameters().size() == 0;
		}
		return false;
	}

	public InformerInfos build(CompilationUnit u) {
		InformerInfos returned = new InformerInfos();
		visit(u, returned);
		return returned;
	}
}

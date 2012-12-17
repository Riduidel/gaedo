package com.dooapp.gaedo.informer;

import java.util.Collection;
import java.util.LinkedList;

import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;


public class ImportResolver<VisitorType> extends VoidVisitorAdapter<VisitorType> {

	/**
	 * Collection of unresolved declared imports
	 */
	protected Collection<String> importStrings = new LinkedList<String>();

	/**
	 * Resolve imports
	 * @param n
	 * @param current
	 * @see japa.parser.ast.visitor.VoidVisitorAdapter#visit(japa.parser.ast.ImportDeclaration, java.lang.Object)
	 */
	@Override
	public void visit(ImportDeclaration n, VisitorType current) {
		// Notice it'se either a class or package name (the .* is removed by javaparser)
		String qualifiedPath = n.getName().toString();
		importStrings.add(qualifiedPath);
	}

}

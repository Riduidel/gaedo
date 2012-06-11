package com.dooapp.gaedo.informer;

import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class EnumScanner extends VoidVisitorAdapter<Void> {
	private String qualifiedName;
	private String packageName;
	
	@Override
	public void visit(PackageDeclaration n, Void arg) {
		this.packageName = n.getName().toString();
	}
	
	@Override
	public void visit(EnumDeclaration n, Void arg) {
		qualifiedName = packageName+"."+n.getName();
	}

	public String getQualifiedName() {
		return qualifiedName;
	}
}

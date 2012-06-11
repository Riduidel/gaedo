package com.dooapp.gaedo.informer;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;

public class RequiredInterfacesScanner extends ScanningMatcher {

	public RequiredInterfacesScanner(String[] requiredInterfaces) {
		super(requiredInterfaces);
	}


	@Override
	public void visit(ClassOrInterfaceDeclaration n, ScanningMatcher current) {
		if(n.getImplements()!=null) {
			for(ClassOrInterfaceType type : n.getImplements()) {
				testElement(type.getName(), current);
			}
		}
	}
}

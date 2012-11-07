package com.dooapp.gaedo.informer;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.expr.AnnotationExpr;


public class RequiredAnnotationsScanner extends ScanningMatcher {

	public RequiredAnnotationsScanner(String[] elements) {
		super(elements);
	}
	
	@Override
	public void visit(ClassOrInterfaceDeclaration n, ScanningMatcher arg) {
		if(n.getAnnotations()!=null) {
			for(AnnotationExpr annotation : n.getAnnotations()) {
				testElement(annotation.getName().toString(), arg);
			}
		}
	}
}

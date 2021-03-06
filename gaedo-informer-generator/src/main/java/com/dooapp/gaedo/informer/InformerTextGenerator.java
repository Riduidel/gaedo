package com.dooapp.gaedo.informer;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.maven.plugin.logging.Log;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.informers.ObjectFieldInformer;
import com.dooapp.gaedo.utils.Utils;

/**
 * Generates the text of the informer generator
 * 
 * @author ndx
 * 
 */
public class InformerTextGenerator {

	public static CompilationUnit generateVisibleInformer(InformerInfos informerInfos, Collection<String> qualifiedEnums, Map<String, Class> resolvedInformers) {
		CompilationUnit cu = new CompilationUnit();
		// set the package
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(informerInfos.classPackage)));

		// Finally add imports (they may have been "improved" by informers generation)
		List<ImportDeclaration> baseImports = informerInfos.imports;
		// Extracting effective imports
		Collection<String> imports = new LinkedList<String>();
		
		// Add current package to import for resolution (but do not forget to remove it before writing the effective imports)
		imports.add(informerInfos.classPackage);
		imports.add("java.lang");
		for (ImportDeclaration d : baseImports) {
			imports.add(d.getName().toString());
		}

		// create the type declaration
		ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, true, informerInfos.getInformerName());
		List<ClassOrInterfaceType> extended = new LinkedList<ClassOrInterfaceType>();
		extended.add(new ClassOrInterfaceType(Informer.class.getSimpleName() + "<" + informerInfos.className + ">"));
		extended.add(new ClassOrInterfaceType(informerInfos.getAbstractInformerName()));
		type.setExtends(extended);
		type.setJavaDoc(new JavadocComment("\n" + 
						"Informer for {@link " + informerInfos.className + "}\n" +
						"This is the public Informer class one must use to access dynamic properties methods." + 
						"@author InformerMojos\n"));
		List<AnnotationExpr> annotations = new LinkedList<AnnotationExpr>();
		// Constructing generated annotation value
		List<MemberValuePair> parameters = new LinkedList<MemberValuePair>();
		parameters.add(new MemberValuePair("date", new StringLiteralExpr(
						javax.xml.bind.DatatypeConverter.printDateTime(GregorianCalendar.getInstance()))));
		parameters.add(new MemberValuePair("comments", new StringLiteralExpr("generated by gaedo-informer-generator")));
		List<Expression> values = new LinkedList<Expression>();
		values.add(new StringLiteralExpr(informerInfos.getQualifiedClassName()));
		parameters.add(new MemberValuePair("value", 
						new ArrayInitializerExpr(values)));
		NormalAnnotationExpr generated = new NormalAnnotationExpr(ASTHelper.createNameExpr(Generated.class.getName()), parameters);
		annotations.add(generated);
		type.setAnnotations(annotations);
		ASTHelper.addTypeDeclaration(cu, type);
		
		imports.remove(informerInfos.classPackage);
		imports.remove("java.lang");

		baseImports = new LinkedList<ImportDeclaration>();
		for(String name : imports) {
			baseImports.add(new ImportDeclaration(ASTHelper.createNameExpr(name), false, false));
		}
		baseImports.add(new ImportDeclaration(ASTHelper.createNameExpr(Informer.class.getCanonicalName()), false, false));
		baseImports.add(new ImportDeclaration(ASTHelper.createNameExpr(ObjectFieldInformer.class.getPackage().getName()), false, true));
		cu.setImports(baseImports);
		return cu;
	}

	public static CompilationUnit generateAbstractInformer(InformerInfos informerInfos, Collection<String> qualifiedEnums, Map<String, Class> resolvedInformers, Log log) {
		CompilationUnit cu = new CompilationUnit();
		// set the package
		cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(informerInfos.classPackage)));

		// Finally add imports (they may have been "improved" by informers generation)
		List<ImportDeclaration> baseImports = informerInfos.imports;
		// Extracting effective imports
		Collection<String> imports = new LinkedList<String>();
		
		// superclasspackage will stay null as long as no import has been found for a class which name ends with ".superClassName"
		// Obvioulsy, if none is found, a warning is written in log
		String superClassPackage = null;
		
		// Add current package to import for resolution (but do not forget to remove it before writing the effective imports)
		imports.add(informerInfos.classPackage);
		imports.add("java.lang");
		String superClassSuffix = null;
		if(informerInfos.superClassName!=null)
			superClassSuffix = "."+informerInfos.superClassName;
		for (ImportDeclaration d : baseImports) {
			String importName = d.getName().toString();
			imports.add(importName);
			if(informerInfos.superClassName!=null) {
				if(importName.endsWith(superClassSuffix)) {
					if(superClassPackage!=null) {
						log.warn("more than one imports of "+informerInfos.getQualifiedClassName()+" contains superclass name. Last one found is "+importName+" This confuses gaedo-informer-generator");
					}
					superClassPackage = importName.substring(0, importName.lastIndexOf(superClassSuffix));
				}
			}
		}

		// create the type declaration
		ClassOrInterfaceDeclaration type = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, true, informerInfos.getAbstractInformerName());
		if(informerInfos.superClassName!=null && !"Object".equals(informerInfos.superClassName)) {
			List<ClassOrInterfaceType> extended = new LinkedList<ClassOrInterfaceType>();
			String superInformer = InformerInfos.buildAbstractInformerName(informerInfos.superClassName);
			// do not forget to add import for that class (see https://github.com/Riduidel/gaedo/issues/20)
			if(superClassPackage!=null) {
				imports.add(superClassPackage+"."+superInformer);
			}
			extended.add(new ClassOrInterfaceType(superInformer));
			type.setExtends(extended);
		}
		type.setJavaDoc(new JavadocComment("\n" + 
						"Informer method container for {@link " + informerInfos.className + "}\n" +
						"This interface is to be used only by gaedo code. Its only role is to provide consistent method hierarchy.\n" +
						"As a consequence, refering to it directly has not the slightest interest and should never been done in user code." + 
						"@author InformerMojos\n"));
		List<AnnotationExpr> annotations = new LinkedList<AnnotationExpr>();
		// Constructing generated annotation value
		List<MemberValuePair> parameters = new LinkedList<MemberValuePair>();
		parameters.add(new MemberValuePair("date", new StringLiteralExpr(
						javax.xml.bind.DatatypeConverter.printDateTime(GregorianCalendar.getInstance()))));
		parameters.add(new MemberValuePair("comments", new StringLiteralExpr("generated by gaedo-informer-generator")));
		List<Expression> values = new LinkedList<Expression>();
		values.add(new StringLiteralExpr(informerInfos.getQualifiedClassName()));
		parameters.add(new MemberValuePair("value", 
						new ArrayInitializerExpr(values)));
		NormalAnnotationExpr generated = new NormalAnnotationExpr(ASTHelper.createNameExpr(Generated.class.getName()), parameters);
		annotations.add(generated);
		type.setAnnotations(annotations);
		ASTHelper.addTypeDeclaration(cu, type);
		for (PropertyInfos infos : informerInfos.properties) {
			// create a method
			MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "get" + Utils.uppercaseFirst(infos.name));
			String informerTypeFor = InformerTypeFinder.getInformerTypeFor(resolvedInformers, qualifiedEnums, imports, infos);
			method.setType(new ClassOrInterfaceType(informerTypeFor));
			method.setJavaDoc(new JavadocComment(infos.generateJavadoc(informerInfos, informerTypeFor)));
			ASTHelper.addMember(type, method);
		}
		
		imports.remove(informerInfos.classPackage);
		imports.remove("java.lang");

		baseImports = new LinkedList<ImportDeclaration>();
		for(String name : imports) {
			baseImports.add(new ImportDeclaration(ASTHelper.createNameExpr(name), false, false));
		}
		baseImports.add(new ImportDeclaration(ASTHelper.createNameExpr(Informer.class.getCanonicalName()), false, false));
		baseImports.add(new ImportDeclaration(ASTHelper.createNameExpr(ObjectFieldInformer.class.getPackage().getName()), false, true));
		cu.setImports(baseImports);
		return cu;
	}

}

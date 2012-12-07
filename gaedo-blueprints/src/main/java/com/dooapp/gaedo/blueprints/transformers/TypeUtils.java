package com.dooapp.gaedo.blueprints.transformers;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TypeUtils {
	private static Map<String, String> typesToClasses = new TreeMap<String, String>();
	private static Map<String, String> classesToTypes = new TreeMap<String, String>();
	
	static {
		Map<String, String> prefixesToUris = new TreeMap<String, String>();
		prefixesToUris.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
		
		Map<String, Class> constructionMap = new HashMap<String, Class>();
//		constructionMap.put("xsd:anyURI", ""); //URI (Uniform Resource Identifier) 
//		constructionMap.put("xsd:base64Binary", ""); //Binary content coded as "base64" 
		constructionMap.put("xsd:boolean", Boolean.class); //Boolean (true or false) 
		constructionMap.put("xsd:byte", Byte.class); //Signed value of 8 bits 
		constructionMap.put("xsd:date", Date.class); //Gregorian calendar date 
		constructionMap.put("http://www.w3.org/2001/XMLSchema#date", Date.class); //Gregorian calendar date 
		constructionMap.put("xsd:dateTime", Date.class); //Instant of time (Gregorian calendar) 
//		constructionMap.put("xsd:decimal", ""); //Decimal numbers 
		constructionMap.put("xsd:double", Double.class); //IEEE 64-bit floating-point 
//		constructionMap.put("xsd:duration", ""); //Time durations 
//		constructionMap.put("xsd:ENTITIES", ""); //Whitespace-separated list of unparsed entity references 
//		constructionMap.put("xsd:ENTITY", ""); //Reference to an unparsed entity 
		constructionMap.put("xsd:float", Float.class); //IEEE 32-bit floating-point 
//		constructionMap.put("xsd:gDay", ""); //Recurring period of time: monthly day 
//		constructionMap.put("xsd:gMonth", ""); //Recurring period of time: yearly month 
//		constructionMap.put("xsd:gMonthDay", ""); //Recurring period of time: yearly day 
//		constructionMap.put("xsd:gYear", ""); //Period of one year 
//		constructionMap.put("xsd:gYearMonth", ""); //Period of one month 
//		constructionMap.put("xsd:hexBinary", ""); //Binary contents coded in hexadecimal 
//		constructionMap.put("xsd:ID", ""); //Definition of unique identifiers 
//		constructionMap.put("xsd:IDREF", ""); //Definition of references to unique identifiers 
//		constructionMap.put("xsd:IDREFS", ""); //Definition of lists of references to unique identifiers 
		constructionMap.put("xsd:int", Integer.class); //32-bit signed integers 
		constructionMap.put("xsd:integer", Integer.class); //Signed integers of arbitrary length 
		constructionMap.put("xsd:language", Locale.class); //RFC 1766 language codes 
		constructionMap.put("xsd:long", Long.class); //64-bit signed integers 
//		constructionMap.put("xsd:Name", ""); //XML 1.O name 
//		constructionMap.put("xsd:NCName", ""); //Unqualified names 
//		constructionMap.put("xsd:negativeInteger", ""); //Strictly negative integers of arbitrary length 
//		constructionMap.put("xsd:NMTOKEN", ""); //XML 1.0 name token (NMTOKEN) 
//		constructionMap.put("xsd:NMTOKENS", ""); //List of XML 1.0 name tokens (NMTOKEN) 
//		constructionMap.put("xsd:nonNegativeInteger", ""); //Integers of arbitrary length positive or equal to zero 
//		constructionMap.put("xsd:nonPositiveInteger", ""); //Integers of arbitrary length negative or equal to zero 
//		constructionMap.put("xsd:normalizedString", ""); //Whitespace-replaced strings 
//		constructionMap.put("xsd:NOTATION", ""); //Emulation of the XML 1.0 feature 
//		constructionMap.put("xsd:positiveInteger", ""); //Strictly positive integers of arbitrary length
//		constructionMap.put("xsd:QName", ""); //Namespaces in XML-qualified names 
		constructionMap.put("xsd:short", Short.class); //32-bit signed integers 
		constructionMap.put("xsd:string", String.class); //Any string 
//		constructionMap.put("xsd:time", ""); //Point in time recurring each day 
//		constructionMap.put("xsd:token", ""); //Whitespace-replaced and collapsed strings 
//		constructionMap.put("xsd:unsignedByte", ""); //Unsigned value of 8 bits 
//		constructionMap.put("xsd:unsignedInt", ""); //Unsigned integer of 32 bits 
//		constructionMap.put("xsd:unsignedLong", ""); //Unsigned integer of 64 bits 
//		constructionMap.put("xsd:unsignedShort", ""); //Unsigned integer of 16 bits
		for(Map.Entry<String, Class> entry : constructionMap.entrySet()) {
			String className = entry.getValue().getName();
			String type = entry.getKey();
			typesToClasses.put(type, className);
			for(Map.Entry<String, String> prefixToUri : prefixesToUris.entrySet()) {
				if(type.startsWith(prefixToUri.getKey())) {
					typesToClasses.put(type.replace(prefixToUri.getKey(), prefixToUri.getValue()), className);
				}
			}
			if(!classesToTypes.containsKey(className))
				classesToTypes.put(className, type);
		}
	}

	public static Object getType(Class<? extends Object> valueClass) {
		String name = valueClass.getName();
		if(classesToTypes.containsKey(name)) {
			return classesToTypes.get(name);
		} else {
			return name;
		}
	}

	public static String getClass(String type) {
		if(typesToClasses.containsKey(type))
			return typesToClasses.get(type);
		else
			return type;
	}

}

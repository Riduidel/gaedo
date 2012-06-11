package com.dooapp.gaedo.informer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;

public class InformerTypeFinder {
	private static final String INFORMER = "Informer";

	/**
	 * Notice this method adds the famous java.lang import to the list of existing ones
	 * @param resolvedInformers basicly resolved informers, those are the default ones
	 * @param imports collection of imports for file. It may be updated by this method
	 * @param infos infos for property
	 * @return a string containing the informer class name.
	 */
	public static String getInformerTypeFor(Map<String, Class> resolvedInformers, Collection<String> qualifiedEnums, Collection<String> imports, PropertyInfos infos) {
		return getNamedInformerTypeFor(resolvedInformers, qualifiedEnums, imports, infos.type.toString());
	}

	public static String getNamedInformerTypeFor(Map<String, Class> resolvedInformers, Collection<String> qualifiedEnums, Collection<String> imports, String type) {
		// Before all, check if it's a wildcarded type
		int genericsOpener = type.indexOf('<');
		if(genericsOpener>0) {
			// values may be either string or map of strings
			Map<String, ?> unwrapped = unwrapGenerics(type);
			return buildNamedInformerFor(resolvedInformers, qualifiedEnums, imports, unwrapped);
		} else {
			return getNamedInformerForRawType(resolvedInformers, qualifiedEnums, imports, type);
		}
	}

	private static String buildNamedInformerFor(Map<String, Class> resolvedInformers, Collection<String> qualifiedEnums, Collection<String> imports, Map<String, ?> unwrapped) {
		StringBuilder sOut = new StringBuilder();
		for(Map.Entry<String, ?> entry : unwrapped.entrySet()) {
			sOut.append(getNamedInformerForRawType(resolvedInformers, qualifiedEnums, imports, entry.getKey()));
			sOut.append("<");
			// Suspended as well
			// See http://gaedo.origo.ethz.ch/issues/41 for the why
			sOut.append(entry.getValue());
/*			if(entry.getValue() instanceof String) {
				sOut.append(getNamedInformerForRawType(resolvedInformers, qualifiedEnums, imports, (String) entry.getValue()));
			} else if(entry.getValue() instanceof Collection) {
				// There is no sub-parameterized type
				Collection c = (Collection) entry.getValue();
				boolean hasComma = false;
				for(Object o : c) {
					if(hasComma==true)
						sOut.append(", ");
					if(o instanceof Map) {
						sOut.append(buildNamedInformerFor(resolvedInformers, qualifiedEnums, imports, (Map<String, ?>) o));
					} else {
						sOut.append(getNamedInformerForRawType(resolvedInformers, qualifiedEnums, imports, (String) o));
					}
					hasComma = true;
					
				}
			}
*/			sOut.append(">");
		}
		return sOut.toString();
	}

	/**
	 * Unwraps a generics statement
	 * @param type
	 * @return
	 */
	private static Map<String, Object> unwrapGenerics(String type) {
		int genericsOpener = type.indexOf('<');
		int genericsCloser = type.lastIndexOf('>');
		Map<String, Object> returned = new HashMap<String, Object>();
		String raw = type.substring(0, genericsOpener);
		String parameters = type.substring(genericsOpener+1, genericsCloser);
		returned.put(raw, parameters);
		
		// For now, informer generation in container subtype is suspended
		// See http://gaedo.origo.ethz.ch/issues/41 for the why
		
//		// Now begins mayhem
//		if(parameters.indexOf(',')>0) {
//			// I hope there is no generics of generics
//			if(parameters.indexOf('<')>0) {
//				throw new UnsupportedOperationException("I'm not good enough in string processing to handle the multi-generic declaration case");
//			} else {
//				returned.put(raw, Arrays.asList(parameters.split(",")));
//			}
//		}
		return returned;
	}

	private static String getNamedInformerForRawType(Map<String, Class> resolvedInformers, Collection<String> qualifiedEnums, Collection<String> imports, String type) {
		type = type.trim();
		// First try direct qualified name
		String returned = getResolvedInformer(resolvedInformers, qualifiedEnums, type);
		if(returned==null) {
			// Always try qualified names first
			for(String s : imports) {
				if(returned==null && s.substring(s.lastIndexOf(".")+1).equals(type)) {
					returned = getResolvedInformer(resolvedInformers, qualifiedEnums, s);
				}
			}
		}
		if(returned==null) {
			// Then wildcarded imports
			for(String s : imports) {
				if(returned==null) {
					returned = getResolvedInformer(resolvedInformers, qualifiedEnums, s+"."+type);
				}
			}
		}
		if(returned==null) {
			returned = type.toString() + INFORMER;
			// This is a raw type, so consider finding the raw type (if possible in imports and adding it)
			Collection<String> possibleTypes = new LinkedList<String>();
			for(String s : imports) {
				if(s.endsWith("."+type)) {
					possibleTypes.add(s+INFORMER);
				}
			}
			imports.addAll(possibleTypes);
		}
		// Then give up
		return returned;
	}

	private static String getResolvedInformer(Map<String, Class> resolvedInformers, Collection<String> qualifiedEnums, String s) {
		if(qualifiedEnums.contains(s)) {
			s = Enum.class.getCanonicalName();
		}
		// Direct import
		if(resolvedInformers.containsKey(s)) {
			Class qualifiedClass = resolvedInformers.get(s);
			// According to implem, all basic informers are in same package
			return BasicFieldInformerLocator.getInformersMapping().get(qualifiedClass).getSimpleName();
		}
		return null;
	}

}

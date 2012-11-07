package com.dooapp.gaedo.rest.server;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Utils {

	/**
	 * Extract from the input map all the params starting with the given prefix
	 * @param filterParamPrefix
	 * @param requestAttributes
	 * @return
	 */
	public static Map<String, Object> getParamsStartingWith(String filterParamPrefix,
			Map<String, Object> requestAttributes) {
		Map<String, Object> returned = new LinkedHashMap<String, Object>();
		for(Map.Entry<String, Object> attr : requestAttributes.entrySet()) {
			if(attr.getKey().startsWith(filterParamPrefix)) {
				returned.put(attr.getKey(), attr.getValue());
			}
		}
		return returned;
	}

	/**
	 * Pattern used by the {@link #split(String)} method
	 */
	static Pattern splitter = Pattern.compile("[\\[\\]]");

	/**
	 * Split string according to splitter and removes empty matches
	 * @param key
	 * @return
	 */
	public static String[] split(String key) {
		String[] splitted = splitter.split(key, -1);
		Collection<String> returned = new LinkedList<String>();
		for(String s : splitted) {
			if(s.length()>0) {
				returned.add(s);
			}
		}
		return returned.toArray(new String[returned.size()]);
	}

	/**
	 * Transform the map of key to values into a tree of key to values.
	 * @param paramsMap a map of parameters in the form "prefix[dimension_1][dimension_2]...[dimension_n]=somerandomvalue" 
	 * @return a map of maps of maps in the form
	 * <ul>
	 * 			<li>dimension_1
	 * 				<ul>
	 * 					<li>dimension_2 (and so on)</li>
	 * 				</ul>
	 * 			</li>
	 * </ul>
	 * Notice that prefix is totally consumed here
	 */
	public static Map<String, Object> getValuesAsTree(Map<String, Object> paramsMap) {
		Map<String, Object> returned = new LinkedHashMap<String, Object>();
		for(Map.Entry<String, Object> param : paramsMap.entrySet()) {
			String[] paramChain = split(param.getKey());
			Map<String, Object> current = returned;
			for (int index = 0; index < paramChain.length; index++) {
				if(index<paramChain.length-1) {
					if(!current.containsKey(paramChain[index])) {
						current.put(paramChain[index], new LinkedHashMap<String, Object>());
					}
					current = (Map<String, Object>) current.get(paramChain[index]);
				} else {
					current.put(paramChain[index], param.getValue());
				}
			}
		}
		return returned;
	}

}

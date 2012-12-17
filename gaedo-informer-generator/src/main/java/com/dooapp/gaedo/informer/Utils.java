package com.dooapp.gaedo.informer;

import java.util.Map;
import java.util.TreeMap;

import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;

public class Utils {

	/**
	 * Shortcut method fastening access to existing informers
	 * @return
	 */
	public static Map<String, Class> createResolvedInformers() {
		Map<String, Class> returned = new TreeMap<String, Class>();
		for(Class c : BasicFieldInformerLocator.getInformersMapping().keySet()) {
			returned.put(c.getCanonicalName(), c);
		}
		return returned;
	}

}

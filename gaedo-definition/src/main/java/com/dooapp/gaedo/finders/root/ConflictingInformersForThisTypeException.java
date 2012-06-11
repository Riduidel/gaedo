package com.dooapp.gaedo.finders.root;

import java.util.Map;

import com.dooapp.gaedo.finders.FieldInformer;

public class ConflictingInformersForThisTypeException extends UnableToLocateInformerForException {

	public ConflictingInformersForThisTypeException(Class<?> clazz,
			Map<Class<?>, Class<? extends FieldInformer>> foundInformers) {
		super(getMessage(clazz, foundInformers));
	}

	private static String getMessage(Class<?> clazz,
			Map<Class<?>, Class<? extends FieldInformer>> foundInformers) {
		StringBuilder sOut = new StringBuilder("there was a problem locating informer\n");
		sOut.append("\t").append("class ").append(clazz.getName()).append(" has no informer, \n\tbut its various implemented interfaces can be associated to more than one informer :");
		for(Map.Entry<Class<?>, Class<? extends FieldInformer>> entry : foundInformers.entrySet()) {
			sOut.append("\n\t").append(entry.getKey().getName()).append("\t=>\t").append(entry.getValue().getName());
		}
		return sOut.toString();
	}

}

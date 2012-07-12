package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.utils.Utils;

/**
 * Utility class factorizing some code
 * @author ndx
 *
 */
public class Transformers {
	
	public static <TransformerType extends Transformer> TransformerType get(TransformerAssociation<TransformerType>[] values, ClassLoader classLoader, String effectiveType) {
		for(TransformerAssociation l : values) {
			if(l.canHandle(classLoader, effectiveType)) {
				return (TransformerType) l.getTransformer();
			}
		}
		throw new ClassIsNotAKnownLiteralException(effectiveType);
	}

	public static <TransformerType extends Transformer> TransformerType get(TransformerAssociation<TransformerType>[] values, Class dataClass) {
		// Notice all primitive types are objectified before all
		if(dataClass.isPrimitive())
			dataClass = Utils.objectify(dataClass);
		for(TransformerAssociation l : values) {
			if(l.getDataClass().isAssignableFrom(dataClass)) {
				return (TransformerType) l.getTransformer();
			}
		}
		throw new ClassIsNotAKnownLiteralException(dataClass);
	}

	public static boolean containsKey(TransformerAssociation[] values, Class<? extends Object> valueClass) {
		for(TransformerAssociation l : values) {
			if(l.getDataClass().isAssignableFrom(valueClass)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsKey(TransformerAssociation[] values, ClassLoader classLoader, String effectiveType) {
		for(TransformerAssociation l : values) {
			if(l.canHandle(classLoader, effectiveType)) {
				return true;
			}
		}
		return false;
	}

}

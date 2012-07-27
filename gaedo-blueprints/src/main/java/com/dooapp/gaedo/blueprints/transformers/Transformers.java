package com.dooapp.gaedo.blueprints.transformers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

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
		throw new ClassIsNotAKnownLiteralException(values, effectiveType);
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
		throw new ClassIsNotAKnownLiteralException(values, dataClass);
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

	public static <TransformerType extends Transformer> TransformerType get(TransformerAssociation<TransformerType>[] values, Type type) {
		if(type instanceof Class) {
			return get(values, (Class) type);
		} else if(type instanceof ParameterizedType) {
			ParameterizedType param = (ParameterizedType) type;
			try {
				return get(values, param.getRawType());
			} catch(ClassIsNotAKnownLiteralException e) {
				// maybe raw type is a collection type, in which case we can see if there is a literal transform for its values
				// which will be useful as collections and maps are automatically exploded by services
				if(param.getRawType() instanceof Class) {
					Class rawClass = (Class) param.getRawType();
					if(Collection.class.isAssignableFrom(rawClass)) {
						return get(values, param.getActualTypeArguments()[0]);
					}
				}
			}
		} else {
			throw new UnsupportedOperationException("This method doesn't support types like "+type);
		}
		throw new ClassIsNotAKnownLiteralException(values, type);
	}

}

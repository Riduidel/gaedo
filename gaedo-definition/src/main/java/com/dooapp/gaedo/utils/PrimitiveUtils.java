package com.dooapp.gaedo.utils;

import java.lang.reflect.Method;

public class PrimitiveUtils {

	/**
	 * Perform dynamic call to the good *Value method, followed by a primitive to object conversion
	 * @param expected
	 * @param usedType
	 * @return
	 */
	public static <NumberType extends Number> NumberType as(Number expected, Class<NumberType> usedType) {
		Class primitive = Utils.primitize(usedType);
		try {
			// Have you notice how Number declares a bunch of *Value methods ? Let's make good use of them !
			Method toCall = expected.getClass().getDeclaredMethod(primitive.getName()+"Value");
			// Some autoboxing always help !
			return usedType.cast(toCall.invoke(expected));
		} catch(Exception e) {
			throw new UnableToTranscodeNumberException("source number "+expected+" can't be transformed to "+usedType, e);
		}
	}

}

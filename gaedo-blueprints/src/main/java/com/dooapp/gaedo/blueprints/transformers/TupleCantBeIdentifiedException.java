package com.dooapp.gaedo.blueprints.transformers;

import java.util.Map;

import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;
import com.dooapp.gaedo.properties.Property;

public class TupleCantBeIdentifiedException extends BluePrintsCrudServiceException {

	public TupleCantBeIdentifiedException() {
	}

	public TupleCantBeIdentifiedException(String message, Throwable cause) {
		super(message, cause);
	}

	public TupleCantBeIdentifiedException(String message) {
		super(message);
	}

	public TupleCantBeIdentifiedException(Throwable cause) {
		super(cause);
	}

	public static TupleCantBeIdentifiedException dueTo(Object tuple, Map<Property, Object> propertiesWithoutIds) {
		StringBuilder message = new StringBuilder("We couldn't create an id for tuple ")
			.append(tuple).append(" due to properties having non valid id\n")
			.append("involved properties are");
		for(Map.Entry<Property, Object> error : propertiesWithoutIds.entrySet()) {
			message.append("\n\t").append(error.getKey()).append("\n\t\thaving as value ").append(error.getValue());
		}
		return new TupleCantBeIdentifiedException(message.toString());
	}

}

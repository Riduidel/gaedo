package com.dooapp.gaedo.rest.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.dynamic.Mode;

public class ReturnTranscriptor {
	private static final String FIND_RANGE_LAST_PARAMETER = "last";
	private static final String FIND_RANGE_FIRST_PARAMETER = "first";
	
	private static Collection<Mode> supportedModes = Collections.unmodifiableCollection(Arrays.asList(Mode.COUNT, Mode.FIND_ONE, Mode.FIND_RANGE));

	/**
	 * Exception thrown when user tries an unsupported operation
	 * @author ndx
	 *
	 */
	public static class UnsupportedModeException extends RestServerException {

		public UnsupportedModeException(Mode usedMode) {
			super("due to obvious bandwidth/DoS/DDoS/univers being not a kind boy, "+usedMode.toString()+" is not supported by our rest server. As usual, it can be replaced by client-side pagin" +
					"\nsupported modes are "+Arrays.toString(supportedModes.toArray()));
		}

		public UnsupportedModeException(String usedMode) {
			super(usedMode+" is not supported by our rest server" +
					"\nsupported modes are "+Arrays.toString(supportedModes.toArray()));
		}
		
	}

	public static final String MODE_PARAMETER = "mode";

	/**
	 * Build return data from a executable statement, and some return parameters
	 * @param statement executable statement to use
	 * @param returnParams effective parameters
	 * @return the value return from statement operating mdoe
	 */
	public Object buildReturn(QueryStatement statement,
			Map<String, Object> returnParams) {
		Map<String, Object> returnMode = Utils.getValuesAsTree(returnParams);
		Map<String, Object> effectiveParams = (Map<String, Object>) returnMode.get(RestServiceParams.RETURN.getPrefix());
		if(effectiveParams==null) {
			effectiveParams = new HashMap<String, Object>();
			effectiveParams.put(MODE_PARAMETER, Mode.FIND_ALL.getPrefix());
		}
		String effectiveMode = effectiveParams.get(MODE_PARAMETER).toString();
		Mode usedMode = null;
		for(Mode m : Mode.values()) {
			if(usedMode==null && m.getPrefix().equals(effectiveMode)) {
				usedMode = m;
			}
		}
		if(usedMode==null) {
			throw new UnsupportedModeException(effectiveMode);
		}
		Collection<Object> parameters = new LinkedList<Object>();
		switch(usedMode) {
		case FIND_RANGE:
			parameters.add(toInt(returnParams, FIND_RANGE_FIRST_PARAMETER, 0));
			parameters.add(toInt(returnParams, FIND_RANGE_LAST_PARAMETER, 0));
			break;
		case COUNT:
		case FIND_ALL:
		case FIND_ONE:
		default:
		}
		return usedMode.execute(statement, parameters.toArray(), System.currentTimeMillis()+"");
	}

	private int toInt(Map<String, Object> returnParams,
			String paramKey, int defaultValue) {
		if(returnParams.containsKey(paramKey))
			return com.dooapp.gaedo.utils.Utils.fromString(returnParams.get(paramKey).toString(), int.class);
		else
			return defaultValue;
	}
}

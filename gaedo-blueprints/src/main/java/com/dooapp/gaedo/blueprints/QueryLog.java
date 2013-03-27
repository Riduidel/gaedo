package com.dooapp.gaedo.blueprints;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A small class centralizing all logs for queries run on graph.
 * Want these logs to be displayed ? Set log level for "com.dooapp.gaedo.blueprints.QueryLog" to FINE/FINER/FINEST/ALL
 * @author ndx
 *
 */
public class QueryLog {
	public static final Level QUERY_LOGGING_LEVEL = Level.FINE;
	public static final Logger logger = Logger.getLogger(QueryLog.class.getName());

}

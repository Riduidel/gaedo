package com.dooapp.gaedo.google.datastore;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.QueryStatement.State;
import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;

/**
 * This property change listener records some time statistics for queries being
 * executed on datastore.
 * 
 * @author ndx
 * 
 */
public class QueryQuotaMonitorer implements PropertyChangeListener {
	private static final Logger logger = Logger.getLogger("quotas");
	private class QueryInfos {
		private String queryId;
		private long start;

		public QueryInfos(String queryId) {
			this.queryId  = queryId;
			this.start = quotaService.getCpuTimeInMegaCycles();
		}

		public String terminate() {
			long end = quotaService.getCpuTimeInMegaCycles();
			long duration = end-start;
			return queryId+" execution time : "+duration+" MegaCycles, or "+quotaService.convertMegacyclesToCpuSeconds(duration)+ " seconds";
		}
	}
	
	/**
	 * Map of inspected queries
	 */
	private Map<QueryStatement<?, ?>, QueryInfos> queries = new WeakHashMap<QueryStatement<?,?>, QueryInfos>();

    QuotaService quotaService = QuotaServiceFactory.getQuotaService();
	/**
	 * A query changes it status
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (QueryStatement.STATE_PROPERTY.equals(evt.getPropertyName())) {
			if (evt.getSource() instanceof QueryStatement) {
				State newState = (State) evt.getNewValue();
				switch (newState) {
				case INITIAL:
				case MATCHING:
					addQuery((QueryStatement<?, ?>) evt.getSource());
					break;
				case SORTING:
					break;
				case EXECUTED:
					removeQuery((QueryStatement<?, ?>) evt.getSource());
				}
			}
		}

	}

	private void removeQuery(QueryStatement<?, ?> source) {
		QueryInfos infos = queries.remove(source);
		if(infos!=null) {
			logger.info(infos.terminate());
		}
	}

	private void addQuery(QueryStatement<?, ?> query) {
		queries.put(query, new QueryInfos(query.getId()));
	}
}

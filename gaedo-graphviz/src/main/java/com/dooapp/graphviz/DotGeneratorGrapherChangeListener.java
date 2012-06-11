package com.dooapp.graphviz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.QueryStatement.State;

/**
 * Log all query statements when a query statement goes into {@link State#SORTING}
 * @author ndx
 *
 */
public class DotGeneratorGrapherChangeListener implements
		PropertyChangeListener {
	private final Collection<String> loggedQueries = new TreeSet<String>();
	private static final Logger logger = Logger.getLogger("graphviz");

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(QueryStatement.STATE_PROPERTY.equals(evt.getPropertyName())) {
			if(evt.getSource() instanceof QueryStatement) {
				if(evt.getNewValue()==QueryStatement.State.EXECUTED) {
					QueryStatement source = (QueryStatement) evt.getSource();
					if(!loggedQueries.contains(source.getId())) {
						loggedQueries.add(source.getId());
						QueryStatementDotGenerator generator = new QueryStatementDotGenerator(source.getId());
						source.accept(generator);
						logger.info("Run query\n"+source.getId()+"\n"+generator.getText());
					}
				}
			}
		}
	}

}

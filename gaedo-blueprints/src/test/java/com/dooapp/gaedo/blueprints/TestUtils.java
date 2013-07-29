package com.dooapp.gaedo.blueprints;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.indexable.IndexableGraphEnvironment;
import com.dooapp.gaedo.blueprints.providers.Neo4j;
import com.dooapp.gaedo.blueprints.providers.Tinker;

/**
 * Utilities for indexable tests
 * @author ndx
 *
 */
public class TestUtils {
	/**
	 * Activate all logging for tests
	 */
	static {
		Logger logger = Logger.getLogger("");
//		logger.setLevel(Level.ALL);
		Handler[] handlers = logger.getHandlers();
		for(Handler h : handlers) {
			h.setLevel(Level.ALL);
		}
	}

	private static final class IndexableEnvironmentProvider implements EnvironmentCreator {
		@Override
		public AbstractGraphEnvironment<?> environmentFor(GraphProvider o) {
			return new IndexableGraphEnvironment(o);
		}
	}

	private static interface EnvironmentCreator {

		AbstractGraphEnvironment<?> environmentFor(GraphProvider o);

	}
	public static final String A = "A";
	public static final String B = "B";
	public static final String C = "C";
	public static final String SOME_NEW_TEXT = "some new text";
	public static final String TAG_TEXT = "tag text";
	public static final String LOGIN_FOR_UPDATE_ON_CREATE = "login for update on create";
	public static final String TEST_TAG_FOR_CREATE_ON_UPDATE = "test tag for create on update";
	public static final long ID_POST_1 = 1;
	public static final long ABOUT_ID = 10;
	public static final String USER_PASSWORD = "user password";
	public static final String USER_LOGIN = "user login";


	/**
	 * Get directory name for indexable graph storage
	 * @param graphDir
	 * @return
	 */
	public static String indexable(String graphDir) {
		return graphDir+"/indexable";
	}

	public static String sail(String graphDir) {
		return graphDir+"/sail";
	}

	public static Collection<Object[]> loadTestProviders() {
		Collection<Object[]> returned = new LinkedList<Object[]>();
//		returned.add(new Object[] { "tinkergraph", new Tinker()});
//		returned.add(new Object[] { "orientgraph", new OrientDB()});
		returned.add(new Object[] { new Neo4j(),10l});
		returned.add(new Object[] { new Neo4j(),1000l});
		returned.add(new Object[] { new Neo4j(),10000l});
		returned.add(new Object[] { new Neo4j(),100000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000000l});
//		returned.add(new Object[] { "neo4jgraph", new Neo4j(),1000000000000l});
		return returned;
	}

	/**
	 * One elemtn collection containing only neo4j
	 * @return
	 */
	public static Collection<GraphProvider> neo4j() {
		Collection<GraphProvider> returned = new LinkedList<GraphProvider>();
		returned.add(new Neo4j());
		return returned;
	}

	public static Collection<GraphProvider> tinker() {
		Collection<GraphProvider> returned = new LinkedList<GraphProvider>();
		returned.add(new Tinker());
		return returned;
	}

	public static Collection<GraphProvider> providers() {
		Collection<GraphProvider> returned = new LinkedList<GraphProvider>();
		returned.addAll(neo4j());
		returned.addAll(tinker());
		return returned;

	}

	public static Collection<Object[]> simpleTestProviders() {
		return simpleTestProviders(providers());
	}

	/**
	 * Transform providers into their arrays counterpart for encapsulation
	 * @param providers
	 * @return
	 */
	public static Collection<Object[]> simpleTestProviders(Collection<GraphProvider> providers) {
		Collection<Object[]> returned = new LinkedList<Object[]>();
		for(GraphProvider gp : providers) {
			returned.add(new Object[] { gp});
		}
		return returned;
	}

	public static Collection<Object[]> loadTest() {
		return environmentsFor(loadTestProviders());
	}

	public static Collection<Object[]> simpleTest() {
		return environmentsFor(simpleTestProviders());
	}

	public static AbstractGraphEnvironment<?> indexable(GraphProvider provider) {
		return new IndexableEnvironmentProvider().environmentFor(provider);
	}

	/**
	 * Create all required environments by replacing {@link GraphProvider} instance with pairs
	 * @param loadTestProviders
	 * @return
	 */
	public static List<Object[]> environmentsFor(Collection<Object[]> providers) {
		List<Object[]> returned = new LinkedList<Object[]>();
		for(Object[] p : providers) {
			returned.add(convertProviderToEnvironment(p, new IndexableEnvironmentProvider()).toArray());
//			returned.add(convertProviderToEnvironment(p, new SailEnvironmentProvider()).toArray());
		}
		return returned;
	}

	private static Collection<Object> convertProviderToEnvironment(Object[] p, EnvironmentCreator creator) {
		Collection<Object> temporary = new LinkedList<Object>();
		for(Object o : p) {
			if(o instanceof GraphProvider) {
				temporary.add(creator.environmentFor((GraphProvider) o));
			} else {
				temporary.add(o);
			}
		}
		return temporary;
	}

	private static IndexableGraphEnvironment environmentFor(GraphProvider o) {
		return new IndexableGraphEnvironment((GraphProvider) o);
	}

	static final String TEST_SEPARATOR = "====================================================================\n";

}

package com.dooapp.gaedo.blueprints;

import java.util.Collection;
import java.util.LinkedList;

import com.dooapp.gaedo.blueprints.providers.Neo4j;
import com.dooapp.gaedo.blueprints.providers.Tinker;

/**
 * Utilities for indexable tests
 * @author ndx
 *
 */
public class TestUtils {

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

	public static Collection<Object[]> loadTest() {
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
	
	public static Collection<Object[]> simpleTest() {
		Collection<Object[]> returned = new LinkedList<Object[]>();
		returned.add(new Object[] { new Tinker()});
//		returned.add(new Object[] { new OrientDB()});
		returned.add(new Object[] { new Neo4j()});
		return returned;
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

}

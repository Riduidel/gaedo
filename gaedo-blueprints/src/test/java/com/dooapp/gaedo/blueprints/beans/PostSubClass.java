package com.dooapp.gaedo.blueprints.beans;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;

import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.User;

public class PostSubClass extends Post {

	public static enum AnotherStateForBug26 {
		PUBLIC, FAMILY

	}

	public AnotherStateForBug26 anotherState;

	/**
	 * A list of pages for multipages messages
	 */
	private Map<Integer, PostSubClass> postPages = new TreeMap<Integer, PostSubClass>();

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH})
	public User creator;


	public PostSubClass() {
		super();
	}

	public PostSubClass(long id, String text, float note, State state, User author) {
		super(id, text, note, state, author);
	}

	public PostSubClass(long i, String string, int j, State public1, User author2, Map<String, String> theseMappings) {
		super(i, string, j, public1, author2, theseMappings);
	}

	/**
	 * @return the postPages
	 * @category getter
	 * @category postPages
	 */
	public Map<Integer, PostSubClass> getPostPages() {
		return postPages;
	}

	/**
	 * @param postPages the postPages to set
	 * @category setter
	 * @category postPages
	 */
	public void setPostPages(Map<Integer, PostSubClass> postPages) {
		this.postPages = postPages;
	}


}
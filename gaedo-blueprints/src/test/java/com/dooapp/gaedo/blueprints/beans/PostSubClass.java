package com.dooapp.gaedo.blueprints.beans;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;

import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.User;

public class PostSubClass extends Post {
	
	public static enum AnotherStateForBug26 {
		PUBLIC
		
	}

	public AnotherStateForBug26 anotherState;
	
	@ManyToOne(cascade={CascadeType.PERSIST})
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
	
	
}
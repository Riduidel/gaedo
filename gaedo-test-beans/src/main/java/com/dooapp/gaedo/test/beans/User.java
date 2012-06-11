/**
 * 
 */
package com.dooapp.gaedo.test.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.dooapp.gaedo.extensions.hierarchy.Child;
import com.dooapp.gaedo.test.beans.specific.Theme;

public class User implements Serializable {
	@Id
	public long id;
	
	private String login;
	
	public String password;
	
	public Theme theme;
	
	/**
	 * A very specific message about that user
	 */
	@OneToOne(cascade={CascadeType.ALL})
	public Message about;

	@Child
	public Collection<Post> posts = new LinkedList<Post>();
	
	public User() {
		
	}
	
	public User withLogin(String login) {
		setLogin(login);
		return this;
	}
	public User withPassword(String password) {
		this.password = password;
		return this;
	}
	
	public User withId(long id) {
		this.id = id;
		return this;
	}
	
	
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id != other.id)
			return false;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		return true;
	}

	public void addPosts(Post...posts) {
		this.posts.addAll(Arrays.asList(posts));
	}
}
package com.dooapp.gaedo.test.beans;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Date;

import javax.persistence.Id;

import com.dooapp.gaedo.extensions.hierarchy.Parent;

/**
 * Make it static to ensure it can be accessed by gaedo-google-datastore
 * specific code
 * 
 * @author Nicolas
 * 
 */
public class Post implements Serializable {
	@Id
	public long id;

	public String text;
	public float note;
	public boolean test;
	public State state = State.PRIVATE;
	@Parent
	public User author;

	public Map<String, String> annotations = new TreeMap<String, String>();
	
	public Set<Tag> tags = new TreeSet<Tag>();
	
	public Date publicationDate;
	
	public Post() {
		
	}
	
	public Post(long id, String text, float note, State state, User author) {
		this.id = id;
		this.text = text;
		this.note = note;
		this.state = state;
		this.author = author;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + Float.floatToIntBits(note);
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		Post other = (Post) obj;
		if (id != other.id)
			return false;
		if (Float.floatToIntBits(note) != Float.floatToIntBits(other.note))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	public Post withAnnotation(String key, String value) {
		annotations.put(key, value);
		return this;
	}

	public Post withText(String string) {
		text = string;
		return this;
	}

	public Post withAuthor(User first) {
		author = first;
		return this;
	}
}
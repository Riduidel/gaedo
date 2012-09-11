package com.dooapp.gaedo.test.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;

import com.dooapp.gaedo.extensions.hierarchy.Parent;

/**
 * A post is the standard content of any kind of blog
 * 
 * @author Nicolas
 * 
 */
public class Post extends Identified implements Serializable, Message {

	private static final Logger logger = Logger.getLogger(Post.class.getName());
	@Column(name="post_text")
	public String text;
	@Column(name="post_note")
	public float note;
	public boolean test;
	public State state = State.PRIVATE;
	@Parent
	public User author;

	public Map<String, String> annotations = new TreeMap<String, String>();
	
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	public Set<Tag> tags = new HashSet<Tag>();
	
	public Date publicationDate = new Date();
	
	public Serializable associatedData;
	
	public Post() {
		
	}
	
	public Post(long id, String text, float note, State state, User author) {
		this.id = id;
		this.text = text;
		this.note = note;
		this.state = state;
		this.author = author;
	}

	public Post(long i, String string, int j, State public1, User author2, Map<String, String> theseMappings) {
		this(i, string, j, public1, author2);
		this.annotations = theseMappings;
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
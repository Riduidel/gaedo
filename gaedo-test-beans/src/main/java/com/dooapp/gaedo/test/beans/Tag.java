package com.dooapp.gaedo.test.beans;

import java.io.Serializable;
import java.util.logging.Logger;

public class Tag extends Identified implements Serializable {
	private static final Logger logger = Logger.getLogger(Tag.class.getName());
	
	private String text;
	
	public Tag parent;
	
	/**
	 * a faked rendering class, used to ensure all works ok with class persistence in graph
	 */
	public Class<?> rendering;
	
	public Tag() {
		
	}
	
	public Tag(long id, String text) {
		this.id = id;
		this.text = text;
	}
	
	public Tag(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @param id new value for #id
	 * @category fluent
	 * @category setter
	 * @category id
	 * @return this object for chaining calls
	 */
	public Tag withId(Long id) {
		this.setId(id);
		return this;
	}

	/**
	 * @return the id
	 * @category getter
	 * @category id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 * @category setter
	 * @category id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((text == null) ? 0 : text.hashCode());
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}

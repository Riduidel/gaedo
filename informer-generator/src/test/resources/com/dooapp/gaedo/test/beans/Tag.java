package com.dooapp.gaedo.test.beans;

import java.io.Serializable;

import javax.persistence.Id;

public class Tag implements Serializable {
	@Id
	private Long id;
	private String text;
	
	public Tag() {
		
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}

package com.dooapp.gaedo.blueprints;

import java.io.Serializable;

public class UnknownSerializable implements Serializable {
	private String text;
	
	/**
	 * @param text new value for #text
	 * @category fluent
	 * @category setter
	 * @category text
	 * @return this object for chaining calls
	 */
	public UnknownSerializable withText(String text) {
		this.setText(text);
		return this;
	}

	/**
	 * @return the text
	 * @category getter
	 * @category text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 * @category setter
	 * @category text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		UnknownSerializable other = (UnknownSerializable) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
	
}
package com.dooapp.gaedo.blueprints.bugs.bug80;

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Some null
 * @author ndx
 *
 */
public class Bug80Value {
	@Id @GeneratedValue private String id;
	private String text;
	private Map<String, Object> elements = new TreeMap<String, Object>();
	/**
	 * @return the id
	 * @category getter
	 * @category id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 * @category setter
	 * @category id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @param id new value for #id
	 * @category fluent
	 * @category setter
	 * @category id
	 * @return this object for chaining calls
	 */
	public Bug80Value withId(String id) {
		this.setId(id);
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
	 * @param text new value for #text
	 * @category fluent
	 * @category setter
	 * @category text
	 * @return this object for chaining calls
	 */
	public Bug80Value withText(String text) {
		this.setText(text);
		return this;
	}
	/**
	 * @return the elements
	 * @category getter
	 * @category elements
	 */
	public Map<String, Object> getElements() {
		return elements;
	}
	/**
	 * @param elements the elements to set
	 * @category setter
	 * @category elements
	 */
	public void setElements(Map<String, Object> elements) {
		this.elements = elements;
	}
}

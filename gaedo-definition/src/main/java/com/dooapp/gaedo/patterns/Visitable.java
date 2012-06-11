package com.dooapp.gaedo.patterns;

/**
 * Interface implemented by all visitable objects
 * @author Nicolas
 *
 */
public interface Visitable<VisitorType extends Visitor> {
	/**
	 * By accepting a visitor, an implementor of this class ensures a method of visitor will be called upon its instance
	 * @param visitor
	 */
	public void accept(VisitorType visitor);
}

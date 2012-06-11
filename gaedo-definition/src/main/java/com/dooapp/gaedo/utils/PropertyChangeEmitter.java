package com.dooapp.gaedo.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Interface implemented by objects able to fire {@link PropertyChangeEvent}
 * 
 * @author ndx
 * 
 */
public interface PropertyChangeEmitter {
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener);

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
	 * @param propertyName
	 * @param listener
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener);

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * This is an optionnal operation, as it clearly is out of general property emitter contract.
	 * @param evt
	 */
    public void firePropertyChange(PropertyChangeEvent evt);
}

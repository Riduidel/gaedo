package com.dooapp.gaedo.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.dooapp.gaedo.utils.PropertyChangeEmitter;

/**
 * Test bean used for both tests
 * @author ndx
 *
 */
public class RootBeanIncludingLeaf implements PropertyChangeEmitter{
	public static interface Names {
		public static String B = "b";
		public static String I = "i";
		public static String S = "s";
	}
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	private boolean b;
	
	private int i;
	
	public LeafBean leef;
	
	@Deprecated
	private String s;
	
	public boolean isB() {
		return b;
	}
	public void setB(boolean b) {
		if(b!=this.b) {
			boolean old = this.b;
			this.b = b;
			support.firePropertyChange(Names.B, old, b);
		}
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		if(i!=this.i) {
			int old = this.i;
			this.i = i;
			support.firePropertyChange(Names.I, old, i);
		}
	}
	
	@Deprecated
	public String getS() {
		return s;
	}
	@Deprecated
	public void setS(String s) {
		if((s==null && this.s!=null) || (s!=null && !s.equals(this.s))) {
			String old = this.s;
			this.s = s;
			support.firePropertyChange(Names.S, old, b);
		}
	}
	@Override
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener( listener);
	}
	@Override
	public void firePropertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		support.removePropertyChangeListener(propertyName, listener);
		
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}
}

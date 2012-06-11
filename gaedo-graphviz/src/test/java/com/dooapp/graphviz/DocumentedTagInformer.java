package com.dooapp.graphviz;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;

/**
 * An example of the interface kind that could be generable by (up-to-come) apt
 * code. This interface will use {@link java.lang.reflect.Proxy} to find the
 * correct field informer.
 * 
 * @author Nicolas
 * 
 */
public interface DocumentedTagInformer extends Informer<DocumentedTag> {
	public StringFieldInformer getText();
}

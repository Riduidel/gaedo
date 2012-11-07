package com.dooapp.gaedo.patterns;

import java.io.ObjectStreamException;

/**
 * Interface used for serialization of objects : all proxies implement it for them to be replaced by their loaded version during serialization process.
 * @author ndx
 *
 */
public interface WriteReplaceable {
	/**
	 * JDK method defined in http://docs.oracle.com/javase/1.3/docs/guide/reflection/proxy.html#serial
	 * @return
	 * @throws ObjectStreamException
	 */
	public Object writeReplace() throws ObjectStreamException;
}

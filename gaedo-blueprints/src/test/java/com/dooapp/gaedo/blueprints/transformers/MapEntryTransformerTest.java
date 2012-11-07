package com.dooapp.gaedo.blueprints.transformers;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dooapp.gaedo.properties.DescribedProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.UnableToSetPropertyException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Make sure one can read and write map key and map value
 * @author ndx
 *
 */
public class MapEntryTransformerTest {
	private static class MapEntryHandler implements InvocationHandler {
		private WriteableKeyEntry entry = new WriteableKeyEntry();

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(entry, args);
		}
		
	}

	private static final String VALUE = "value";
	private static final String KEY = "key";
	private MapEntryTransformer tested;
	private Map<Property, Collection<CascadeType>> testedProps;

	@Before
	public void setUp() throws Exception {
		tested = (MapEntryTransformer) Tuples.get(Map.Entry.class);
		testedProps = tested.getContainedProperties();
	}

	@Test
	public void testReadProperties() {
		WriteableKeyEntry used = (WriteableKeyEntry) tested.instanciateTupleFor(getClass().getClassLoader(), null /* Yeah, I abuse here the vertex usage, but it's for the good */);
		used.setKey(KEY);
		used.setValue(VALUE);
		for(Property p : testedProps.keySet()) {
			if(p.getName().equals("key")) {
				assertThat(p.get(used), Is.is((Object) KEY)); 
			} else if(p.getName().equals("value")) {
				assertThat(p.get(used), Is.is((Object) VALUE)); 
			}
		}
	}

	@Test
	public void testWriteProperties() {
		WriteableKeyEntry used = (WriteableKeyEntry) tested.instanciateTupleFor(getClass().getClassLoader(), null /* Yeah, I abuse here the vertex usage, but it's for the good */);
		for(Property p : testedProps.keySet()) {
			if(p.getName().equals("key")) {
				p.set(used, KEY);
			} else if(p.getName().equals("value")) {
				p.set(used, VALUE);
			}
		}
		assertThat(used.getKey(), Is.is((Object) KEY)); 
		assertThat(used.getValue(), Is.is((Object) VALUE)); 
	}

	@Test(expected=UnableToSetPropertyException.class)
	public void testWriteOnNonWriteableKey() throws Exception {
		Entry used = (Entry) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Map.Entry.class}, new MapEntryHandler());
		try {
			for(Property p : testedProps.keySet()) {
				if(p.getName().equals("key")) {
					p.set(used, KEY);
				} else if(p.getName().equals("value")) {
					p.set(used, VALUE);
				}
			}
			fail("shouldn't reach that point, as we're unable to write key");
		} catch(UnableToSetPropertyException e) {
			assertThat(e.getCause(), Is.is(IllegalArgumentException.class));
			throw e;
		}
	}

	/**
	 * This is observed symptom for bug https://github.com/Riduidel/gaedo/issues/5 ... but why ? and how ?
	 */
	@Test @Ignore
	public void testWritePropertiesWithKeySetterRemoved() throws SecurityException, IntrospectionException, NoSuchMethodException {
		WriteableKeyEntry used = (WriteableKeyEntry) tested.instanciateTupleFor(getClass().getClassLoader(), null /* Yeah, I abuse here the vertex usage, but it's for the good */);
		// Some hard replace on properties
		for(Property p : testedProps.keySet()) {
			if(p.getName().equals("key")) {
				// rebuild property without a setter for key
				Class<Map.Entry> interfaceClass = Map.Entry.class; 
				Class<WriteableKeyEntry> implementationClass = WriteableKeyEntry.class;
				PropertyDescriptor keyProperty = new PropertyDescriptor("key", interfaceClass.getDeclaredMethod("getKey"), 
								null);
				p = new DescribedProperty(keyProperty, Map.Entry.class);
				p.set(used, KEY);
			}
		}
	}
}

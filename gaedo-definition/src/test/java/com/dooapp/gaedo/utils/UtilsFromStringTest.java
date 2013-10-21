package com.dooapp.gaedo.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * Test that loading works correctly
 * @author ndx
 *
 */
public class UtilsFromStringTest {

	@Test
	public void canReadAnInt() {
		Object value = Utils.fromString("1", Integer.class);
		assertThat(value, IsInstanceOf.instanceOf(Integer.class));
		assertThat((Integer) value, Is.is(1));
	}

	@Test
	public void canReadAFloat() {
		Object value = Utils.fromString("1.0", Float.class);
		assertThat(value, IsInstanceOf.instanceOf(Float.class));
		assertThat((Float) value, Is.is(1.0f));
	}

	@Test
	public void canReadAClass() {
		Object value = Utils.fromString("java.lang.String", Class.class);
		assertThat(value, IsInstanceOf.instanceOf(Class.class));
		assertThat(value, Is.is((Object) String.class));
	}

	@Test
	public void canReadAString() {
		String text = "text";
		Object value = Utils.fromString(text, String.class);
		assertThat(value, IsInstanceOf.instanceOf(String.class));
		assertThat((String) value, Is.is(text));
	}

	@Test
	public void canReadAnURI() throws URISyntaxException {
		String uri = "http://wwww.perigee.fr";
		Object value = Utils.fromString(uri, URI.class);
		assertThat(value, IsInstanceOf.instanceOf(URI.class));
		assertThat((URI) value, Is.is(new URI(uri)));
	}
}

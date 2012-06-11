package com.dooapp.gaedo.utils;

import java.util.Collection;
import java.util.LinkedList;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test collection and map generated here are the expected ones
 * @author ndx
 *
 */
public class UtilsTest {
	@Test
	public void createCollection() {
		Collection<Object> returned = Utils.generateCollection(Collection.class, null);
		Assert.assertThat(returned, Is.is(LinkedList.class));
	}
}

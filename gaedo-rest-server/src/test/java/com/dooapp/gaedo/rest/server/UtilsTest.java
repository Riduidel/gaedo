package com.dooapp.gaedo.rest.server;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;


public class UtilsTest {
	
	/**
	 * Test that map extraction method works correctly
	 */
	@Test
	public void testStartingWith() {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("a", "b");
		values.put(" a", " b");
		Map<String, Object> tested = Utils.getParamsStartingWith("a", values);
		Assert.assertThat(tested.size(), Is.is(1));
		Assert.assertThat(tested.get("a").toString(), Is.is("b"));
	}
	
	@Test
	public void testGetValuesAsTree() {
		Map<String, Object> testData = new LinkedHashMap<String, Object>();
		testData.put("prefix[a][b][c]", "a/b/c");
		Map<String, Object> values = Utils.getValuesAsTree(testData);
		Assert.assertThat(((Map) ((Map) ((Map) values.get("prefix")).get("a")).get("b")).get("c").toString(), 
				Is.is("a/b/c"));
	}
	
	@Test
	public void testSplitting() {
		String[] result = Utils.split("[a]");
		Assert.assertArrayEquals(result, new String[] {"a"});
		result = Utils.split("[a][b]");
		Assert.assertArrayEquals(result, new String[] {"a", "b"});
	}

}

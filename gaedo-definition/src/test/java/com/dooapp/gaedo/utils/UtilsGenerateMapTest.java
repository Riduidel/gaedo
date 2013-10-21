package com.dooapp.gaedo.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class UtilsGenerateMapTest {

	@Test
	public void canGenerateASortedMap() {
		Map generated = Utils.generateMap(TreeMap.class, null);
		assertThat((Object) generated, IsInstanceOf.instanceOf(SortedMap.class));
	}


	@Test
	public void canGenerateAHashMapMap() {
		Map generated = Utils.generateMap(HashMap.class, null);
		assertThat((Object) generated, IsInstanceOf.instanceOf(Map.class));
	}

	/**
	 * Strange, no ? In fact, this should not happen under normal circumstances
	 */
	@Test
	public void canGenerateANonSortedMapEventIfRequiredTo() {
		Map generated = Utils.generateMap(SortedMap.class, new HashMap());
		assertThat((Object) generated, IsInstanceOf.instanceOf(Map.class));
		assertThat((Object) generated, IsNot.not(IsInstanceOf.instanceOf(SortedMap.class)));
	}
}

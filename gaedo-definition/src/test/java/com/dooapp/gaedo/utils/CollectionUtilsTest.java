package com.dooapp.gaedo.utils;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Test;

import static com.dooapp.gaedo.utils.CollectionUtils.compareCollections;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class CollectionUtilsTest {
	public static class CompareCollectionsTests {
		@Test
		public void ensureItWorksOnTwoEmptyCollections() {
			assertThat(compareCollections(new LinkedList<String>(), new LinkedList<String>()), is(0));
		}
		@Test
		public void ensureItWorksOnOneEmptyCollection() {
			assertThat(compareCollections(new LinkedList<String>(), asList("a")), is(-1));
		}
		@Test
		public void ensureItWorksOnOneEmptyAsSecondParameterCollection() {
			assertThat(compareCollections(asList("a"), new LinkedList<String>()), is(1));
		}
		@Test
		public void ensureItWorksOnOneEmptyIterable() {
			assertThat(compareCollections((Iterable<String>) new LinkedList<String>(), (Iterable<String>) asList("a")), is(-1));
		}
		@Test
		public void ensureItWorksOnOneEmptyIterableAsSecondParameter() {
			assertThat(compareCollections((Iterable<String>) asList("a"), (Iterable<String>) new LinkedList<String>()), is(1));
		}
		@Test
		public void ensureItWorksOnTwooneElementIterables() {
			assertThat(compareCollections((Iterable<String>) asList("a"), (Iterable<String>) asList("a")), is(0));
		}
		@Test
		public void ensureItWorksOnTwoTwoElementIterables() {
			assertThat(compareCollections((Iterable<String>) asList("a", "a"), (Iterable<String>) asList("a", "b")), is(-1));
		}
		@Test
		public void ensureItWorksOnTwoTwoElementIterablesWithDifferentStartValues() {
			assertThat(compareCollections((Iterable<String>) asList("a", "a"), (Iterable<String>) asList("b", "a")), is(-1));
		}
	}
}

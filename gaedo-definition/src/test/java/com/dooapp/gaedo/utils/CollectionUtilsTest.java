package com.dooapp.gaedo.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class CollectionUtilsTest {
	public static class CompareTests {
		/**
		 * Just some type-safe use of Arrays method
		 * @param values
		 * @return
		 */
		public static List<String> list(String ...values) {
			return Arrays.asList(values);
		}

		public static Iterable<String> iterable(String...values) {
			return list(values);
		}

		@Test
		public void canCompareTwoEmptyIterables() {
			assertThat(CollectionUtils.compare(iterable(), iterable()), is(0));
		}


		@Test
		public void canCompareLeftEmptyIterableWithRightNonEmptyIterable() {
			assertThat(CollectionUtils.compare(iterable(), iterable("a")), is(-1));
		}

		@Test
		public void canCompareLeftNonEmptyIterableWithRightEmptyIterable() {
			assertThat(CollectionUtils.compare(iterable("a"), iterable()), is(1));
		}

		@Test
		public void canCompareLeftNonEmptyIterableWithRightEqualsIterable() {
			assertThat(CollectionUtils.compare(iterable("a"), iterable("a")), is(0));
		}

		@Test
		public void canCompare_a_to_a_a() {
			assertThat(CollectionUtils.compare(iterable("a"), iterable("a", "a")), is(-1));
		}

		@Test
		public void canCompare_a_b_to_b_a() {
			assertThat(CollectionUtils.compare(iterable("a","b"), iterable("b","a")), is(-1));
		}

		@Test
		public void canCompare_b_a_to_a_b() {
			assertThat(CollectionUtils.compare(iterable("b","a"), iterable("a","b")), is(1));
		}
	}
}

package com.dooapp.gaedo.utils;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static com.dooapp.gaedo.utils.PrimitiveUtils.as;

import static org.junit.Assert.assertThat;

public class PrimitiveUtilsTest  {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void longToFloat() {
		assertThat((Float) as(2l, Float.class), Is.is(2.0f));
	}


	@Test
	public void floatToLong() {
		assertThat((Long) as(2.1f, Long.class), Is.is(2l));
	}
}

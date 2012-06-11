package com.dooapp.gaedo.guice;

import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;

import com.dooapp.gaedo.finders.dynamic.ServiceGenerator;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Simple test verifying all things works
 * @author ndx
 *
 */
public class GaedoModuleTest {
	@Test
	public void testInjector() {
		Injector injector = Guice.createInjector(new GaedoModule());
		ServiceGenerator g = (ServiceGenerator) injector.getInstance(ServiceGenerator.class);
		Assert.assertThat(g, IsNull.notNullValue());
	}
}

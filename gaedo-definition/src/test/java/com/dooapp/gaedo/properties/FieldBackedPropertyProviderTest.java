package com.dooapp.gaedo.properties;

import java.util.Map;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class FieldBackedPropertyProviderTest {
	private PropertyProvider tested;
	@Before
	public void prepare() {
		tested = new FieldBackedPropertyProvider();
	}
	/**
	 * Test that there are 5 fields (the three known, and the support one
	 */
	@Test
	public void testFieldsCount() {
		Property[] p = tested.get(RootBean.class);
		Assert.assertThat(p.length, Is.is(4));
	}

	@Test
	public void testGetAnnotation() {
		Property[] p = tested.get(RootBean.class);
		Map<String, Property> props = PropertyProviderUtils.asMap(p);
		Deprecated value = props.get(RootBean.Names.S).getAnnotation(Deprecated.class);
		Assert.assertThat(value, IsNull.notNullValue());
	}

	@Test
	public void testValueSet() {
		Property[] p = tested.get(RootBean.class);
		Map<String, Property> props = PropertyProviderUtils.asMap(p);
		RootBean b = new RootBean();
		// set value of i to be 2
		props.get(RootBean.Names.I).set(b, 2);
		Assert.assertThat(b.getI(), Is.is(2));
	}

	@Test
	public void testValueGet() {
		Property[] p = tested.get(RootBean.class);
		Map<String, Property> props = PropertyProviderUtils.asMap(p);
		RootBean b = new RootBean();
		String toto = "toto";
		b.setS(toto);
		// set value of i to be 2
		Assert.assertThat((String) props.get(RootBean.Names.S).get(b), Is.is(toto));
	}
}

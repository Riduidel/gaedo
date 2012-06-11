package com.dooapp.gaedo.finders.root;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.LeafBean;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.RootBean;
import com.dooapp.gaedo.properties.RootBeanIncludingLeaf;

/**
 * It appear there were not the slightest test for this class. As a consequence, this class tests some things about this code.
 * @author ndx
 *
 */
public class BouncingCallsFailTest {
	
	public static interface LeafBeanInformer extends Informer<LeafBean> {
		StringFieldInformer getName();
	}
	
	public static interface BaseBeanInformer extends Informer<RootBeanIncludingLeaf> {
		LeafBeanInformer getLeef();
	}

	/**
	 * Tested object
	 */
	private InformerFactory tested;
	
	@Before
	public void prepare() throws SecurityException, NoSuchFieldException {
		// default property provider
		PropertyProvider provider = new FieldBackedPropertyProvider();
		// default field locator ... well, not so default, however "quite simple"
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		InformerFieldLocator informers = new InformerFieldLocator();
		locator.add(informers);
		// reflection source
		ReflectionBackedInformerFactory reflective = new ReflectionBackedInformerFactory(locator, provider); 
		tested = informers.masquerade(new ProxyBackedInformerFactory(reflective));
	}
	
	/**
	 * Test that all is correctly working when trying to use bean informer with I interface (which do not rely upon unknown class)
	 */
	@Test
	public void dontBounce() {
		LeafBeanInformer beanInformer = tested.get(LeafBeanInformer.class, LeafBean.class);
		Assert.assertThat(beanInformer, IsInstanceOf.instanceOf(Informer.class));
		Assert.assertThat(beanInformer.getName(), IsInstanceOf.instanceOf(StringFieldInformer.class));
		Property name = beanInformer.getName().getField();
		Assert.assertThat(beanInformer.getName().getFieldPath(), IsCollectionContaining.hasItems(name));
	}
	
	/**
	 * Test that all is correctly working when trying to use bean informer with I interface (which do not rely upon unknown class)
	 */
	@Test
	public void bounceOne() {
		LeafBeanInformer leafBeanInformer = tested.get(LeafBeanInformer.class, LeafBean.class);
		BaseBeanInformer beanInformer = tested.get(BaseBeanInformer.class, RootBeanIncludingLeaf.class);
		Assert.assertThat(beanInformer.getLeef(), IsInstanceOf.instanceOf(FieldInformer.class));
		Assert.assertThat(beanInformer.getLeef(), IsInstanceOf.instanceOf(Informer.class));
		Property leef = beanInformer.getLeef().getField();
		Assert.assertThat(beanInformer.getLeef().getName(), IsInstanceOf.instanceOf(StringFieldInformer.class));
		Property name = beanInformer.getLeef().getName().getField();
		Assert.assertThat(beanInformer.getLeef().getName().getFieldPath(), IsCollectionContaining.hasItems(leef, name));
	}
}

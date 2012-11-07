package com.dooapp.gaedo.finders.root;

import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.informers.DoubleFieldInformer;
import com.dooapp.gaedo.finders.informers.LongFieldInformer;
import com.dooapp.gaedo.finders.informers.ObjectFieldInformer;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.LeafBean;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.RootBean;

/**
 * It appear there were not the slightest test for this class. As a consequence, this class tests some things about this code.
 * @author ndx
 *
 */
public class ProxyBackedInformerFactoryTest {
	public static interface BeanInformerWithIMissingParameter extends Informer<RootBean> {
		ObjectFieldInformer getAParameterThatDontExists();
	}
	/**
	 * An informer interface with an invalid return type parameter for I
	 * @author ndx
	 *
	 */
	public static interface BeanInformerWithIAsLong extends Informer<RootBean> {
		LongFieldInformer getI();
	}
	/**
	 * An informer interface with working parameter type for i
	 * @author ndx
	 *
	 */
	public static interface BeanInformerWithIAsDouble extends Informer<RootBean> {
		DoubleFieldInformer getI();
	}

	/**
	 * Tested object
	 */
	private AbstractInformerFactory tested;
	
	@Before
	public void prepare() {
		// default property provider
		PropertyProvider provider = new FieldBackedPropertyProvider();
		// default field locator ... well, not so default, however "quite simple"
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		// reflection source
		ReflectionBackedInformerFactory reflective = new ReflectionBackedInformerFactory(locator, provider); 
		tested = new ProxyBackedInformerFactory(reflective);
	}
	
	/**
	 * Test that all is correctly working when doing nothing with a wrongly created informer (as method names resolution is done lazily (which is a mistake)
	 */
	@Test(expected=MethodConstructedOnMissingField.class)
	public void testBeanInformerWithMissingParameterSucceedAtDeclaration() {
		BeanInformerWithIMissingParameter beanInformer = tested.get(BeanInformerWithIMissingParameter.class, RootBean.class);
		Assert.assertThat(beanInformer, IsNull.notNullValue());
	}
	
	/**
	 * Test that failure is coming when field does not exists in class
	 */
	@Test(expected=MethodConstructedOnMissingField.class)
	public void testBeanInformerWithMissingParameterFailsOninvocation() {
		BeanInformerWithIMissingParameter beanInformer = tested.get(BeanInformerWithIMissingParameter.class, RootBean.class);
		Assert.assertThat(beanInformer.getAParameterThatDontExists(), IsInstanceOf.instanceOf(ObjectFieldInformer.class));
	}
	
	/**
	 * Test that an exception will be thrown when trying to make use of a I as a Long
	 */
	@Test(expected=ReturnTypeMismatchException.class)
	public void testBeanInformerWithIAsLong() {
		BeanInformerWithIAsLong beanInformer = tested.get(BeanInformerWithIAsLong.class, RootBean.class);
		Assert.assertThat(beanInformer.getI(), IsInstanceOf.instanceOf(LongFieldInformer.class));
	}
	
	/**
	 * Test that all is correctly working when trying to use bean informer with I interface (which do not rely upon unknown class)
	 */
	@Test
	public void testBeanInformerWithIAsDouble() {
		BeanInformerWithIAsDouble beanInformer = tested.get(BeanInformerWithIAsDouble.class, RootBean.class);
		Assert.assertThat(beanInformer.getI(), IsInstanceOf.instanceOf(DoubleFieldInformer.class));
	}
}

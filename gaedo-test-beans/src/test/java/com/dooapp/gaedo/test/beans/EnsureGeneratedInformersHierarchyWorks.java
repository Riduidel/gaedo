package com.dooapp.gaedo.test.beans;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for https://github.com/Riduidel/gaedo/issues/14
 * @author ndx
 *
 */
public class EnsureGeneratedInformersHierarchyWorks {

	/**
	 * issue #14 requires there exist a way Post objects have their parent classes informed, and not really that they totally implement parent class associated interface.
	 * A detail ? Well, not so !
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		assertThat(PostInformer.class.getMethod("getId"), IsNull.notNullValue());
	}

}

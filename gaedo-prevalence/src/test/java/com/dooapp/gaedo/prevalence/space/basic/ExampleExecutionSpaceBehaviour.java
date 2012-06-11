package com.dooapp.gaedo.prevalence.space.basic;

import java.io.Serializable;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.prevalence.space.ExecutionSpace;
import com.dooapp.gaedo.prevalence.space.commands.CommandExecutionException;
import com.dooapp.gaedo.prevalence.space.commands.Contains;
import com.dooapp.gaedo.prevalence.space.commands.Create;
import com.dooapp.gaedo.prevalence.space.commands.Delete;
import com.dooapp.gaedo.prevalence.space.commands.Get;


public class ExampleExecutionSpaceBehaviour {
	/**
	 * Tested executiopn space
	 */
	private ExecutionSpace<String> tested;
	
	@Before
	public void prepare() {
		tested = new SimpleExecutionSpace<String>();
	}
	
	/**
	 * As this test left space unchanged, it can theorically be run multiple times without issues
	 */
	@Test
	public void doSomething() {
		// First, create a simple object
		String firstKey = "first object key";
		String firstData = "some serializable data";
		// First execution is an obvious success
		tested.execute(new Create<String>(firstKey, firstData));
		// Second execution should fail
		try {
			tested.execute(new Create<String>(firstKey, firstData));
			Assert.fail("an exception should have been thrown here");
		} catch(CommandExecutionException e) {
			// Nothing to do, as it is the normal behaviour
		}
		Assert.assertThat(tested.execute(new Get<String>(firstKey)), Is.is((Object) firstData));
		// Now, delete entry
		Assert.assertThat(tested.execute(new Delete<String>(firstKey)), Is.is((Object) firstData));
		// And ensure it no more is in
		Assert.assertThat(tested.execute(new Contains<String>(firstKey)), Is.is(false));
	}
}

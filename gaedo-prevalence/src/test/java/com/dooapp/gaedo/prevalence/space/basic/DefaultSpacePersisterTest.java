package com.dooapp.gaedo.prevalence.space.basic;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dooapp.gaedo.prevalence.space.Command;
import com.dooapp.gaedo.prevalence.space.commands.Create;


public class DefaultSpacePersisterTest {
	/**
	 * Test path used to isolate the storage
	 */
	private static final String testPathName = DefaultSpacePersisterTest.class.getSimpleName();
	
	private static File testPathFile;

	private DefaultSpacePersister tested;
	
	@BeforeClass
	public static void createPath() {
		testPathFile = new File(".", testPathName);
		if(!testPathFile.exists()) {
			testPathFile.mkdirs();
		}
		// Ensure test data is deleted at exit
		testPathFile.deleteOnExit();
	}
	
	@Before
	public void prepare() {
		tested = new DefaultSpacePersister(testPathFile);
	}
	
	/**
	 * First command is simply name "command_0.log"
	 */
	@Test
	public void testCreateCommandLogFileName() {
		for(int i: Arrays.asList(0,25,256,789)) {
			Assert.assertThat(tested.buildCommandLogFileName(i).getName(), Is.is(i+".commandLog"));
		}
	}
	
	/**
	 * Test that a logged command can be read
	 */
	@Test
	public void testReadWriteCommand() {
		Create<Serializable> create = new Create<Serializable>("key", "value");
		int COMMAND_INDEX = 0;
		tested.writeCommand(COMMAND_INDEX, create);
		Command<?, ?> read = tested.readCommand(COMMAND_INDEX);
		Assert.assertEquals(read, create);
	}
	
	/**
	 * Test correct behaviour of logCommand for a bunch of commands to log
	 */
	@Test public void testLogCommand() {
		for(int i=0; i<100; i++) {
			Create<Serializable> create = new Create<Serializable>("key", i);
			tested.logCommand(create);
			Command<?, ?> read = tested.readCommand(i);
			Assert.assertEquals(read, create);
		}
	}
}

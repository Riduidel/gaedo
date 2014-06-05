package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.finders.FindPostByText;
import com.dooapp.gaedo.blueprints.operations.Loader.LoadProperties;
import com.dooapp.gaedo.test.beans.Post;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor81_CanEnableSelectiveLoggingOnOperations extends AbstractGraphPostTest {
	public static class TestFor81PostSubClass extends Post {
		@OneToOne(cascade={CascadeType.PERSIST})
		public String onlyOnCreate;
		public TestFor81PostSubClass() {}
	}
	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor81_CanEnableSelectiveLoggingOnOperations(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void can_change_log_level_for_update_with_ease() {
		String METHOD_NAME = getClass().getName()+"#can_change_log_level_for_update_with_ease";
		TestFor81PostSubClass nonUpdatable = new TestFor81PostSubClass();
		nonUpdatable.text = METHOD_NAME;
		nonUpdatable.onlyOnCreate = METHOD_NAME;
		getPostService().create(nonUpdatable);
		// field is write-only, so it is not read
		// there should be a message written by Updater
		Logger loader = Logger.getLogger(LoadProperties.class.getName());
		loader.setLevel(Level.ALL);
		loader.addHandler(new Handler() {

			@Override
			public void publish(LogRecord record) {
				assertThat(record.getLoggerName(), Is.is(LoadProperties.class.getName()));
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {
			}
		});
		// now mock some log handler to check one message is written
		nonUpdatable = (TestFor81PostSubClass) getPostService().find().matching(new FindPostByText(METHOD_NAME)).getFirst();
		assertThat(nonUpdatable.onlyOnCreate, IsNull.nullValue());
	}
}

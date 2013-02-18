package com.dooapp.gaedo.blueprints.bugs.supernodes;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.test.beans.Post;

import static com.dooapp.gaedo.blueprints.TestUtils.environmentsFor;
import static com.dooapp.gaedo.blueprints.TestUtils.simpleTestProviders;
import static com.dooapp.gaedo.blueprints.TestUtils.neo4j;

import static org.junit.Assert.assertThat;

/**
 * parallel performance test making sure environment doesn't open/close used connections.
 * Notice all the load/unload have been removed from test lifecycle due to multithreading issues
 * @author ndx
 *
 */
public class StandardMultiwritePerformanceAndSearchTest extends AbstractGraphPostSubClassTest {

	private static final Logger logger = Logger.getLogger(StandardMultiwritePerformanceAndSearchTest.class.getName());
	
    @Rule
    public ContiPerfRule i = new ContiPerfRule();
    
    private AtomicInteger sessions = new AtomicInteger(0); 
    private AtomicInteger tests = new AtomicInteger(0); 

	public StandardMultiwritePerformanceAndSearchTest() {
		// use first possible test
		super((AbstractGraphEnvironment<?>) environmentsFor(simpleTestProviders(neo4j())).get(0)[0]);
	}

	@Before
	public void loadService() throws Exception {
	}

	private void doLoadService() throws Exception {
		int sessionCount;
		if((sessionCount = sessions.getAndIncrement())==0) {
			super.loadService();
			logger.info("loaded service");
		}
		logger.info("UP to "+sessionCount);
	}

	@After
	public synchronized void unload() throws Exception {
	}

	private void doUnloadService() throws Exception {
		int sessionCount;
		if((sessionCount = sessions.decrementAndGet())==0) {
			super.unload();
			logger.info("unloaded service");
		}
		logger.info("DOWN to "+sessionCount);
	}

	@Test
    @PerfTest(invocations = 100, threads = 10)
	public void aPostCanBeCreatedAndDeletedFast() throws Exception {
		doLoadService();
		Post p = new Post();
		p.author = author;
		p.annotations.put("test", getClass().getName());
		p.text = "created on "+new Date()+" as "+tests.incrementAndGet();
		synchronized(environment) {
			p = getPostService().create(p);
		}
		assertThat(p.id, IsNot.not(0l));
		synchronized(environment) {
			getPostService().delete(p);
		}
		logger.info("test over for "+p.text);
		doUnloadService();
	}
}

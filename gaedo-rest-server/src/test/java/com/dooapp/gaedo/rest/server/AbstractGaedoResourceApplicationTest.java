package com.dooapp.gaedo.rest.server;

import org.junit.Before;
import org.junit.Ignore;
import org.restlet.Component;
import org.restlet.data.Protocol;

import com.dooapp.gaedo.finders.repository.ServiceRepository;

@Ignore
public abstract class AbstractGaedoResourceApplicationTest {

	protected static final int TEST_PORT = 8182;
	protected static final String TEST_DIR = "/gaedo";
	protected ServiceRepository repository;
	private Component component;

	@Before
	public void prepare() throws Exception {
		repository = TestEnvironmentProvider.create();
	
	    // Create a new Component.   
	    component = new Component();   
	
	    // Add a new HTTP server listening on port 8182.   
	    component.getServers().add(Protocol.HTTP, TEST_PORT);   
	
	    // Attach the sample application.   
	    component.getDefaultHost().attach(TEST_DIR,   
	            new GaedoResourceApplication(repository));   
	
	    // Start the component.   
		component.start();
	}

	public void shutdown() throws Exception {
		component.stop();
	}

}

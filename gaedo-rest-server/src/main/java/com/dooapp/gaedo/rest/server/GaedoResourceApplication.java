package com.dooapp.gaedo.rest.server;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.dooapp.gaedo.finders.repository.ServiceRepository;

public class GaedoResourceApplication extends Application {
	public static final String SERVICE_LIST_ROUTE = "/services";
	private ServiceRepository repository;
	
	public GaedoResourceApplication(ServiceRepository repository) {
		setRepository(repository);
	}

	public ServiceRepository getRepository() {
		return repository;
	}

	public void setRepository(ServiceRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		// Add route for services description
		router.attach(SERVICE_LIST_ROUTE, ServicesList.class);
		
		// Add magic route for each service
		router.attach(SERVICE_LIST_ROUTE+"/{"+RestServiceFacade.CONTAINED_TYPE_ATTRIBUTE+"}/", RestServiceFacade.class);
		return router;
	}
}

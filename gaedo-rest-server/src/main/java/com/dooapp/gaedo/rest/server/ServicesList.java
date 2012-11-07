package com.dooapp.gaedo.rest.server;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.dynamic.DynamicFinder;
import com.dooapp.gaedo.finders.dynamic.DynamicFinderHandler;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.root.InformerClassInvocationHandler;

/**
 * Provide general view over services
 * 
 * @author ndx
 * 
 */
public class ServicesList extends ServerResource {
	public static final String SERVICE_NODE_NAME = "service";
	public static final String SERVICE_LIST_NODE_NAME = "services";
	public static final String SERVICE_TYPE_ATTRIBUTE = "serviceType";
	public static final String INFORMER_TYPE_ATTRIBUTE = "informerType";
	public static final String DATA_TYPE_ATTRIBUTE = "dataType";
	private static final Logger logger = Logger
			.getLogger(ServicesList.class.getName());
	/**
	 * Small interface allowing fast and easy JSON and XML publishing of services infos
	 * @author ndx
	 *
	 */
	private interface ServicePublisher {
		/**
		 * Gives the type of contained obejcts
		 * @param containedClass
		 */
		void setDataType(Class<?> containedClass);
		/**
		 * Gives the informer type
		 * @param informerClass
		 */
		void setInformerType(Class<? extends Object> informerClass);

		/**
		 * Gives the servcie type
		 * @param class1
		 */
		void setServiceType(Class<? extends FinderCrudService> serviceClass);
		/**
		 * Start publicating one service infos
		 */
		void startPublish();
		/**
		 * Stop publicating one service infos
		 */
		void endPublish();

	}
	
	private class XmlResourcePublisher implements ServicePublisher {
		private Element rootElement;
		private Document document;
		private Element serviceElement;

		public XmlResourcePublisher(Element rootElt, Document d) {
			this.rootElement = rootElt;
			this.document = d;
		}

		public void setDataType(Class<?> containedClass) {
			serviceElement.setAttribute(DATA_TYPE_ATTRIBUTE, containedClass.getName());
		}

		@Override
		public void setInformerType(Class<? extends Object> informerClass) {
			serviceElement.setAttribute(INFORMER_TYPE_ATTRIBUTE, informerClass.getName());
		}

		@Override
		public void setServiceType(Class<? extends FinderCrudService> serviceClass) {
			serviceElement.setAttribute(SERVICE_TYPE_ATTRIBUTE, serviceClass.getName());
			
		}

		@Override
		public void endPublish() {
			serviceElement = null;
		}

		@Override
		public void startPublish() {
			serviceElement = document.createElement(SERVICE_NODE_NAME);
			rootElement.appendChild(serviceElement);
		}
		
	}
	
	private class JsonResourcePublisher implements ServicePublisher {
		private class JsonDoesNotWorksException extends RestServerException {
			public JsonDoesNotWorksException(Exception source) {
				super(source);
			}
		}
		private JSONArray collector = new JSONArray();
		private JSONObject current;

		@Override
		public void endPublish() {
			collector.put(current);
			current = null;
		}

		@Override
		public void setDataType(Class<?> containedClass) {
			try {
				current.put(DATA_TYPE_ATTRIBUTE, containedClass.getName());
			} catch (JSONException e) {
				throw new JsonDoesNotWorksException(e); 
			}
		}

		@Override
		public void setInformerType(Class<? extends Object> informerClass) {
			try {
				current.put(INFORMER_TYPE_ATTRIBUTE, informerClass.getName());
			} catch (JSONException e) {
				throw new JsonDoesNotWorksException(e); 
			}
		}

		@Override
		public void setServiceType(
				Class<? extends FinderCrudService> serviceClass) {
			try {
				current.put(SERVICE_TYPE_ATTRIBUTE, serviceClass.getName());
			} catch (JSONException e) {
				throw new JsonDoesNotWorksException(e); 
			}
		}

		@Override
		public void startPublish() {
			current = new JSONObject();
		}

		public String toJSON() {
			return collector.toString();
		}
		
	}

	/**
	 * Get service list as an xml document
	 * @return
	 * @throws IOException
	 */
	@Get("xml")
	public Representation getXmlServicesList() throws IOException {
        DomRepresentation representation = new DomRepresentation(   
                MediaType.TEXT_XML);   
        // Generate a DOM document representing the item.   
        Document d = representation.getDocument();   
  
		Element rootItem = d.createElement(SERVICE_LIST_NODE_NAME);   
		d.appendChild(rootItem);   
		ServicePublisher publisher = new XmlResourcePublisher(rootItem, d);
 
   		publishAllServcies(publisher);
        d.normalizeDocument();   
        
        // Returns the XML representation of this document.   
        return representation;   
	}
	
	/**
	 * Json representation, for the lightweight boys. It replies when user wants json content
	 * @return
	 */
	@Get("json")
	public Representation getJsonServicesList() {
		JsonResourcePublisher publisher = new JsonResourcePublisher();
		publishAllServcies(publisher);
		JsonRepresentation representation = new JsonRepresentation(publisher.toJSON());
		return representation;
	}

	/**
	 * Publish all services infos. Or, to be more precise, all id based servcies infos
	 * @param publisher used publisher
	 */
	private void publishAllServcies(ServicePublisher publisher) {
		ServiceRepository repository = ((GaedoResourceApplication) getApplication()).getRepository();
		for(FinderCrudService<?, ?> service : repository.values()) {
			if(service instanceof IdBasedService) {
				if(Serializable.class.isAssignableFrom(service.getContainedClass())) {
					publishService(service, publisher);
				} else {
					logger.log(Level.WARNING, "unable to publish service for non serializable class "+service.getContainedClass());
				}
			} else {
				logger.log(Level.WARNING, "unable to publish non IdBasedService "+service);
			}
		}
	}

	/**
	 * Publish service infos
	 * @param service
	 * @param resourcePublisher
	 */
	private void publishService(FinderCrudService<?, ?> service,
			ServicePublisher resourcePublisher) {
		resourcePublisher.startPublish();
		if(service instanceof DynamicFinder) {
			// A dynamic finder is a proxy
			InvocationHandler handler = Proxy.getInvocationHandler(service);
			if (handler instanceof DynamicFinderHandler) {
				DynamicFinderHandler dynamicHandler = (DynamicFinderHandler) handler;
				resourcePublisher.setServiceType(dynamicHandler.getToImplement());
			}
		} else {
			// We suppose here interface is a direct implementor of FinderCrudServcie (which may be totally false)
			resourcePublisher.setServiceType(FinderCrudService.class);
		}
		resourcePublisher.setDataType(service.getContainedClass());
		if(service.getInformer() instanceof Proxy) {
			InvocationHandler handler = Proxy.getInvocationHandler(service.getInformer());
			if (handler instanceof InformerClassInvocationHandler) {
				InformerClassInvocationHandler informerHandler = (InformerClassInvocationHandler) handler;
				resourcePublisher.setInformerType(informerHandler.getInformerClass());
			}
		} else {
			resourcePublisher.setInformerType(service.getInformer().getClass());
		}
		resourcePublisher.endPublish();
	}
}

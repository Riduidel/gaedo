package com.dooapp.gaedo.rest.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.service.ConverterService;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.Property;

/**
 * Presents a facade for a classical gaedo service using all available
 * representations
 * 
 * @author ndx
 * 
 */
public class RestServiceFacade extends ServerResource {
	/**
	 * Attribute key to get the contained type data.
	 */
	public static final String CONTAINED_TYPE_ATTRIBUTE = "containedType";
	private static final Logger logger = Logger.getLogger(RestServiceFacade.class.getName());
	/**
	 * Maximum size of collection returnable through REST connection
	 */
	private static final int MAX_COLLECTION_SIZE = 100;

	/**
	 * Transcript sort params into a sorting filter
	 */
	private SortingTranscripter sortingTranscripter = new SortingTranscripter();
	/**
	 * transcript filter params into query statement
	 */
	private QueryTranscripter queryTranscripter = new QueryTranscripter();
	/**
	 * Transcript query statement result into effective result
	 */
	private ReturnTranscriptor returnTranscriptor = new ReturnTranscriptor();
	
	/**
	 * Put a new object in a given service.
	 * This operation is to be transcripted as {@link FinderCrudService#create(Object)}
	 * @category REST
	 * @return the representation of the effectively created object
	 */
	@Post
	@SuppressWarnings("unchecked") /* Removed since working with typesafe generics is useless here */
	public Representation put() {
		String containedType = getRequestAttributes().get(
				CONTAINED_TYPE_ATTRIBUTE).toString();
		try {
			Class<?> containedClass = Class.forName(containedType);
			FinderCrudService service = getServiceRepository().get(containedClass);
			Map<String, Object> objectParams = RestServiceParams.OBJECT.getParams(getRequestAttributes());
			Object returned = create(service, objectParams);
			return represent(returned);
		} catch(Exception e) {
			return handleExceptionRestReturn("unable to get data from service associated to "+containedType, e);
		}
	}

	/**
	 * Create given object from specified arguments
	 * @param service
	 * @param objectParams
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @category CRUD
	 */
	public Object create(FinderCrudService service,
			Map<String, Object> objectParams) throws InstantiationException,
			IllegalAccessException {
		Map<String, Object> valuesAsTree = (Map<String, Object>) Utils.getValuesAsTree(objectParams).get(RestServiceParams.OBJECT.getPrefix());
		Object created = service.getContainedClass().newInstance();
		Informer<?> informer = service.getInformer();
		for(Object field : informer.getAllFields()) {
			if (field instanceof Property) {
				Property property = (Property) field;
				if(valuesAsTree.containsKey(property.getName())) {
					property.set(created, property.fromString(valuesAsTree.get(property.getName()).toString()));
				}
			}
		}
		return service.create(created);
	}

	/**
	 * The allmighty get method aggregates three distinct features of gaedo :
	 * {@link FinderCrudService#findAll()}, {@link FinderCrudService#find()} and
	 * {@link IdBasedService#findById(Object...)}. It does so by providing a
	 * rigourous analysis of the parameters given as input to it.
	 * 
	 * This analysis is split in various steps.
	 * <ol>
	 * <li>First, the contained type is retrieved (rather easy, since restlet
	 * creates the variable for us)</li>
	 * <li>Then search properties are looked up. Their lookup is done the
	 * following way: They all start with {@value #FILTER_PARAM_PREFIX} and are supposed to be map of
	 * properties of input bean. As an example, for the User class of gaedo test
	 * beans, "login", "password", "id" and "posts" are valid names. However,
	 * there are special cases. If any of {@link FilterAggregator} are used, they generate associated query expressions.</li>
	 * <li>In the same way, sort parameters are defined by using {@value #SORT_PARAM_PREFIX} as
	 * prefix, and "Ascending" or "Descending" as value.</li>
	 * <li>Finally, the search operation is given the same way using the {@value #RETURN_PARAM_PREFIX} parameter</li>
	 * </ol>
	 * 
	 * @return
	 * @category REST
	 */
	@Get
	@SuppressWarnings("unchecked") /* Removed since working with typesafe generics is useless here */
	public Representation find() {
		String containedType = getRequestAttributes().get(
				CONTAINED_TYPE_ATTRIBUTE).toString();
		try {
			Class<?> containedClass = Class.forName(containedType);
			FinderCrudService service = getServiceRepository().get(containedClass);
			Map<String, Object> filterParams = RestServiceParams.FILTER.getParams(getRequestAttributes());
			Map<String, Object> sortParams = RestServiceParams.SORT.getParams(getRequestAttributes());
			Map<String, Object> returnParams = RestServiceParams.RETURN.getParams(getRequestAttributes());
			Object returnable = find(service, filterParams, sortParams, returnParams);
			return represent(returnable);
		} catch(Exception e) {
			return handleExceptionRestReturn("unable to get data from service associated to "+containedType, e);
		}
	}

	/**
	 * Handle an exception to be transcripted as a REST return
	 * @param containedType
	 * @param e
	 * @return
	 */
	private Representation handleExceptionRestReturn(String message,
			Exception e) {
		AppendableRepresentation error = buildErrorRepresentation(e,
				message);
		logger.log(Level.WARNING, message, e);
		return error;
	}

	/**
	 * Creates a representation from any kind of object.
	 * @param source source object to represent
	 * @return an XML representation built from a modified XML serialization mechanism
	 * @todo replace with use of {@link ConverterService}
	 */
	private Representation represent(Object source) {
		if(source instanceof Serializable) {
			return new ObjectRepresentation<Serializable>((Serializable) source);
		} else if(source instanceof Iterable) {
			// As a matter of fact, collections representation are all limited to MAX_COLLECTION_SIZE
			Collection<Object> returned = new LinkedList<Object>();
			int index = 0;
			for(Object o : (Iterable) source) {
				if(index<MAX_COLLECTION_SIZE)
					returned.add(o);
				else
					break;
			}
			return represent(returned);
		} else {
			throw new UnrepresentableObjectException(source);
		}
	}

	/**
	 * Testable method (for checking all works correctly outside of the restlet box)
	 * @param service
	 * @param filterParams
	 * @param sortParams
	 * @param returnParams
	 * @return
	 * @category CRUD
	 */
	public Object find(FinderCrudService service,
			Map<String, Object> filterParams, Map<String, Object> sortParams,
			Map<String, Object> returnParams) {
		// TODO find a way to make use of IdBasedService, because that's obviously not the case here
		QueryStatement statement =  service.find().matching(queryTranscripter.buildQuery(service, filterParams));
		statement = statement.sortBy(sortingTranscripter.buildSorting(service, sortParams));
		return returnTranscriptor.buildReturn(statement, returnParams);
	}

	/**
	 * Create an error representation and returns it
	 * @param e
	 * @param message
	 * @return
	 */
	private AppendableRepresentation buildErrorRepresentation(Exception e,
			String message) {
		AppendableRepresentation error = new AppendableRepresentation(message);
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(errorStream));
		try {
			error.append("\n\nerror stack\n\n");
			error.append(errorStream.toString());
		} catch(Exception x) {
			logger.log(Level.SEVERE, "unable to append anything to error representation", x);
		}
		return error;
	}

	/**
	 * Getter for servcie repository, accessing it from {@link GaedoResourceApplication#getRepository()}
	 * @category getter
	 * @return current instance of ServiceRepository
	 */
	private ServiceRepository getServiceRepository() {
		return ((GaedoResourceApplication) getApplication()).getRepository();
		}
}

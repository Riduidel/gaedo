package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FieldInformerAPI;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.EqualsExpression;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;

public class ReflectionBackedInformer<DataType> implements Informer<DataType> {
	private static final Logger logger = Logger.getLogger(ReflectionBackedInformer.class.getName());
	/**
	 * Class used when a {@link ReflectionBackedInformer} is seen as a field of another object
	 * @author ndx
	 *
	 */
	public class AsFieldInformer implements Informer<DataType> {
		/**
		 * Field used to see the containing {@link ReflectionBackedInformer}
		 */
		private Property field;
		/**
		 * Stored parent path
		 */
		private List<Property> parentPath = Collections.emptyList();

		public AsFieldInformer(Property field, List<Property> parentPath) {
			super();
			this.field = field;
			this.parentPath = parentPath;
		}

		public AsFieldInformer(Property field) {
			this.field = field;
		}

		@Override
		public FieldInformer get(String string, Collection<Property> propertyPath) {
			FieldInformer returned = ReflectionBackedInformer.this.get(string);
			if(returned instanceof FieldInformerAPI) {
				// Improve path with this informer one
				Collection<Property> newPath = new LinkedList<Property>(propertyPath);
				newPath.add(field);
				returned = ((FieldInformerAPI) returned).with(newPath);
			}
			return returned;
		}

		@Override
		public FieldInformer get(String string) {
			return get(string, parentPath);
		}

		@Override
		public QueryExpression equalsTo(Object value) {
			return new EqualsExpression(field, getFieldPath(), value);
		}

		@Override
		public Informer asField(Property field) {
			return ReflectionBackedInformer.this.asField(field);
		}

		@Override
		public Property getField() {
			return field;
		}

		@Override
		public Collection<FieldInformer> getAllFieldInformers() {
			return ReflectionBackedInformer.this.getAllFieldInformers();
		}

		@Override
		public Collection<Property> getAllFields() {
			return ReflectionBackedInformer.this.getAllFields();
		}

		@Override
		public Iterable<Property> getFieldPath() {
			List<Property> returned  = new LinkedList<Property>(parentPath);
			returned.add(field);
			return returned;
		}

		/**
		 * @return
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			return result;
		}

		/**
		 * @param obj
		 * @return
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AsFieldInformer other = (AsFieldInformer) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			return true;
		}

		private ReflectionBackedInformer getOuterType() {
			return ReflectionBackedInformer.this;
		}

		@Override
		public FieldInformer with(Collection propertyPath) {
			return new AsFieldInformer(field, new LinkedList<Property>(propertyPath));
		}
	}

	/**
	 * Informer for super class
	 */
	private Informer<? super DataType> parent;

	/**
	 * Local class, used for getting fields and properties
	 */
	private final Class<DataType> informedClass;

	/**
	 * Map linking known properties to effective informers. Notice this map is to be lazily loaded by {@link #loadFieldsInformers(ReflectionBackedInformerFactory)}
	 */
	private Map<Property, FieldInformer> fields;

	private PropertyProvider propertyProvider;

	/**
	 * Informer factory used for lazy loading the field informers
	 */
	private ReflectionBackedInformerFactory informerFactory;

	public ReflectionBackedInformer(Class<DataType> clazz,
			ReflectionBackedInformerFactory reflectionBackedInformerFactory,
			PropertyProvider provider) {
		// Immediatly load parent infos
		if(!clazz.isAssignableFrom(Object.class)) {
			parent = reflectionBackedInformerFactory.get(clazz.getSuperclass());
		}
		// Now, get all fields
		this.informedClass = clazz;
		this.propertyProvider = provider;
		this.informerFactory = reflectionBackedInformerFactory;
	}

	/**
	 * Load all fields informers from the class fields
	 * @param reflectionBackedInformerFactory
	 */
	private Map<Property, FieldInformer> loadFieldsInformers(ReflectionBackedInformerFactory reflectionBackedInformerFactory) {
		Map<Property, FieldInformer> futureFields = new HashMap<Property, FieldInformer>();
		Class<?> currentClass = informedClass;
		while(!Object.class.equals(currentClass)) {
			Property[] fieldsArray = propertyProvider.get(currentClass);
			for(Property f : fieldsArray) {
				// notice only non-static non-transient fields will have associated informers, as static fields have their values shared amongst all class instances
				// and transient are, by design, transient (and not intended to be persisted)
				if(f.hasModifier(Modifier.STATIC)) {
					logger.fine("field "+f.toGenericString()+" will be ignored as it is a static one");
				} else if(f.hasModifier(Modifier.TRANSIENT)) {
					logger.fine("field "+f.toGenericString()+" will be ignored as it is a transient one");
				} else {
					try {
						futureFields.put(f, reflectionBackedInformerFactory.getInformerFor(f));
					} catch(UnsupportedOperationException e) {
						e.printStackTrace();
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return futureFields;
	}

	/**
	 * Effective implementation obtaining field with given assignated property path
	 * @param string field name
	 * @param propertyPath parent path
	 * @return a {@link FieldInformer} or an exception
	 * @see #internalGet(String)
	 */
	@Override
	public FieldInformer get(String string, Collection<Property> propertyPath) {
		FieldInformer returned = internalGet(string);
		if(returned instanceof FieldInformerAPI) {
			returned = ((FieldInformerAPI) returned).with(new LinkedList<Property>(propertyPath));
		}
		return returned;
	}

	/**
	 * Obtain field informer for the given property name
	 * @param string
	 * @return a {@link FieldInformer} or an exception
	 * @see #get(String, Collection)
	 */
	@Override
	public FieldInformer get(String string) {
		return get(string, new LinkedList<Property>());
	}


	/**
	 * Obtain entry by performing an informed lookup consisting into first looking up field name then field name qualified with class simple name, and finally by performing
	 * lookup in parent class
	 * @param string field name
	 * @return a {@link FieldInformer} or an exception
	 */
	public FieldInformer internalGet(String string) {
		if(fields==null) {
			fields = loadFieldsInformers(informerFactory);
		}
		for(Map.Entry<Property, FieldInformer> f : fields.entrySet()) {
			if(f.getKey().getName().equals(string)) {
				return f.getValue();
			} else if((informedClass.getSimpleName()+"."+f.getKey().getName()).equals(string)) {
				return f.getValue();
			}
		}
		if(parent!=null) {
			try {
				return parent.get(string);
			} catch(NoSuchFieldInHierarchyException e) {
				/* we do nothing here. I perfeclty know integrating exception in application logic is not optimal, but it's, to my mind, the best way to achieve the right result here. */
			}
		}
		return informerFactory.noSuchFieldInHiearchy(this, string);
	}

	/**
	 * The equalsTo method, as implemented by {@link Informer}, checks that the informer reference is equals to the reference given.
	 * As a consequence, a null value is given for the field (which is an error since it does not allows model navigation)
	 */
	@Override
	public QueryExpression equalsTo(Object value) {
		return new EqualsExpression(null, new LinkedList<Property>(), value);
	}

	@Override
	public Informer asField(Property field) {
		return new AsFieldInformer(field);
	}

	/**
	 * There is no field associated with object informer. As a consequence, an exception is thrown
	 */
	@Override
	public Property getField() {
		throw new UnsupportedOperationException("No field can be associated to a ReflectionBackedInformer, which only describes a root object");
	}

	/**
	 * Get all fields of this object. This method creates a short lifetime collection containing all fields of this object (coming from this class and from superclass)
	 * @return
	 */
	public Collection<FieldInformer> getAllFieldInformers() {
		Collection<FieldInformer> toReturn = new LinkedList<FieldInformer>();
		if(parent!=null) {
			toReturn.addAll(parent.getAllFieldInformers());
		}
		if(fields==null) {
			fields = loadFieldsInformers(informerFactory);
		}
		toReturn.addAll(fields.values());
		return toReturn;
	}

	@Override
	public Collection<Property> getAllFields() {
		Collection<Property> toReturn = new LinkedList<Property>();
		if(parent!=null) {
			toReturn.addAll(parent.getAllFields());
		}
		if(fields==null) {
			fields = loadFieldsInformers(informerFactory);
		}
		toReturn.addAll(fields.keySet());
		return toReturn;
	}

	/**
	 * As itself, that class has an empty field path
	 * @return
	 * @see com.dooapp.gaedo.finders.FieldInformer#getFieldPath()
	 */
	@Override
	public Iterable<Property> getFieldPath() {
		return Collections.emptyList();
	}

	@Override
	public FieldInformer with(Collection propertyPath) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+FieldInformerAPI.class.getName()+"#use has not yet been implemented AT ALL");
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((informedClass == null) ? 0 : informedClass.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReflectionBackedInformer other = (ReflectionBackedInformer) obj;
		if (informedClass == null) {
			if (other.informedClass != null)
				return false;
		} else if (!informedClass.getCanonicalName().equals(other.informedClass.getCanonicalName()))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReflectionBackedInformer [");
		if (informedClass != null) {
			builder.append("clazz=");
			builder.append(informedClass.getName());
			builder.append(", ");
		}
		if (fields != null) {
			builder.append("fields=");
			builder.append(fields.keySet());
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the informedClass
	 * @category getter
	 * @category informedClass
	 */
	public Class<DataType> getInformedClass() {
		return informedClass;
	}
}

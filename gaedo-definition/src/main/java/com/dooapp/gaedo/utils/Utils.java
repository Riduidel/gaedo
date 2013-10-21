package com.dooapp.gaedo.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.properties.Property;

public class Utils {

	/**
	 * From an input container type, and a possibly null input value, generates a Map corresponding to the container interface kind
	 * @param rawContainerClass this is the expected type of the map (TreeMap, HashMap, anymap)
	 * @param property this is the current map value. it can be null
	 * @return property if non null, an instance of rawContainerClass elsewhere
	 */
	@SuppressWarnings("rawtypes")
	public static Map<?, ?> generateMap(Class<?> rawContainerClass, Map<?, ?> property) {
		if (property == null) {
			if (SortedMap.class.isAssignableFrom(rawContainerClass)) {
				property = new TreeMap();
			} else if (Map.class.isAssignableFrom(rawContainerClass)) {
				property = new HashMap();
			}
		}
		return property;
	}

	/**
	 * Generate an instance of one of the concrete types corresponding to input
	 * type (if input type is not cocnrete)
	 *
	 * @param rawContainerType
	 * @param property
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <DataType> Collection<DataType> generateCollection(Class<?> rawContainerType,
			Collection<DataType> property) {
		if (property == null) {
			if (rawContainerType.isInterface()) {
				if (BlockingQueue.class.isAssignableFrom(rawContainerType)) {
					property = new ArrayBlockingQueue<DataType>(10);
				} else if (Queue.class.isAssignableFrom(rawContainerType)) {
					property = new ArrayBlockingQueue<DataType>(10);
				} else if (NavigableSet.class.isAssignableFrom(rawContainerType)) {
					property = new TreeSet<DataType>();
				} else if (Set.class.isAssignableFrom(rawContainerType)) {
					property = new HashSet<DataType>();
				} else if (SortedSet.class.isAssignableFrom(rawContainerType)) {
					property = new TreeSet<DataType>();
				} else if (List.class.isAssignableFrom(rawContainerType)) {
					property = new LinkedList<DataType>();
				} else if (Collection.class.isAssignableFrom(rawContainerType)) {
					property = new LinkedList<DataType>();
				}
			} else if (Modifier.isAbstract(rawContainerType.getModifiers())) {
				throw new UnsupportedOperationException(
						"are you kiddin or what ? Replace your abstract type "
								+ rawContainerType.getName()
								+ "by an interface, it's wayyyy better !");
			} else {
				try {
					property = (Collection<DataType>) rawContainerType.newInstance();
				} catch (Exception e) {
					throw new UnableToCreateObjectException(e, rawContainerType);
				}
			}
		}
		return property;
	}

	/**
	 * Maybe transform type in object one. if type is not a primitive, nothing is done
	 * @param toObjectify
	 * @return
	 */
	public static Class<?> maybeObjectify(Class<?> toObjectify) {
		Class<?> returned = objectify(toObjectify);
		if(returned==null)
			returned = toObjectify;
		return returned;
	}

	/**
	 * Apply the {@link #maybeObjectify(Class)} algorithm to a string containing a class name
	 * @param toObjectify class name to objectify
	 * @return
	 */
	public static String maybeObjectify(String toObjectify) {
		String returned = objectify(toObjectify);
		if(returned==null)
			returned = toObjectify;
		return returned;
	}

	/**
	 * Transform a primitive type into its associated class : Integer.Type will become Integer.class, and so on ...
	 * @param toCompareClass
	 * @return
	 */
	public static String objectify(String toCompareClass) {
		if(Integer.TYPE.getName().equals(toCompareClass)) {
			return Integer.class.getName();
		} else if(Long.TYPE.getName().equals(toCompareClass)) {
			return Long.class.getName();
		} else if(Short.TYPE.getName().equals(toCompareClass)) {
			return Short.class.getName();
		} else if(Float.TYPE.getName().equals(toCompareClass)) {
			return Float.class.getName();
		} else if(Double.TYPE.getName().equals(toCompareClass)) {
			return Double.class.getName();
		} else if(Byte.TYPE.getName().equals(toCompareClass)) {
			return Byte.class.getName();
		} else if(Character.TYPE.getName().equals(toCompareClass)) {
			return Character.class.getName();
		} else if(Boolean.TYPE.getName().equals(toCompareClass)) {
			return Boolean.class.getName();
		}
		return null;
	}

	/**
	 * Transform an object type into associated primitive type
	 * @param toCompareClass
	 * @return
	 */
	public static String primitize(String toCompareClass) {
		if(Integer.class.getName().equals(toCompareClass)) {
			return Integer.TYPE.getName();
		} else if(Long.class.getName().equals(toCompareClass)) {
			return Long.TYPE.getName();
		} else if(Short.class.getName().equals(toCompareClass)) {
			return Short.TYPE.getName();
		} else if(Float.class.getName().equals(toCompareClass)) {
			return Float.TYPE.getName();
		} else if(Double.class.getName().equals(toCompareClass)) {
			return Double.TYPE.getName();
		} else if(Byte.class.getName().equals(toCompareClass)) {
			return Byte.TYPE.getName();
		} else if(Character.class.getName().equals(toCompareClass)) {
			return Character.TYPE.getName();
		} else if(Boolean.class.getName().equals(toCompareClass)) {
			return Boolean.TYPE.getName();
		}
		return null;
	}

	/**
	 * Transform an object type into associated primitive type
	 * @param toCompareClass
	 * @return
	 */
	public static Class<?> primitize(Class<? extends Number> toCompareClass) {
		if(Integer.class.equals(toCompareClass)) {
			return Integer.TYPE;
		} else if(Long.class.equals(toCompareClass)) {
			return Long.TYPE;
		} else if(Short.class.equals(toCompareClass)) {
			return Short.TYPE;
		} else if(Float.class.equals(toCompareClass)) {
			return Float.TYPE;
		} else if(Double.class.equals(toCompareClass)) {
			return Double.TYPE;
		} else if(Byte.class.equals(toCompareClass)) {
			return Byte.TYPE;
		} else if(Character.class.equals(toCompareClass)) {
			return Character.TYPE;
		} else if(Boolean.class.equals(toCompareClass)) {
			return Boolean.TYPE;
		}
		return null;
	}

	/**
	 * Transform a primitive type into its associated class : Integer.Type will become Integer.class, and so on ...
	 * @param toCompareClass
	 * @return
	 */
	public static Class<?> objectify(Class<?> toCompareClass) {
		if(Integer.TYPE.equals(toCompareClass)) {
			return Integer.class;
		} else if(Long.TYPE.equals(toCompareClass)) {
			return Long.class;
		} else if(Short.TYPE.equals(toCompareClass)) {
			return Short.class;
		} else if(Float.TYPE.equals(toCompareClass)) {
			return Float.class;
		} else if(Double.TYPE.equals(toCompareClass)) {
			return Double.class;
		} else if(Byte.TYPE.equals(toCompareClass)) {
			return Byte.class;
		} else if(Character.TYPE.equals(toCompareClass)) {
			return Character.class;
		} else if(Boolean.TYPE.equals(toCompareClass)) {
			return Boolean.class;
		}
		return null;
	}

	/**
	 * Generates a map linking uppercased field name to lwoercased one from a list of properties objects
	 */
	public static Map<String, String> getUppercasedMap(Property[] fields) {
		Map<String, String> fieldNames = new HashMap<String, String>();
		for(Property f : fields) {
			String name = f.getName();
			fieldNames.put(uppercaseFirst(name), name);
		}
		return fieldNames;
	}

	/**
	 * Build a map linking method names to their associated object.
	 * As a convenience, methods declared by Object class are excluded from the returned map
	 * @param methods object visible methods (public ones)
	 * @return a map linking their first-upcased letter name to the {@link Method} object
	 */
	public static Map<String, Method> getUppercasedMap(Method[] methods) {
		Map<String, Method> fieldNames = new HashMap<String, Method>();
		for(Method method : methods) {
			if(!method.getDeclaringClass().equals(Object.class)) {
				String name = method.getName();
				fieldNames.put(uppercaseFirst(name), method);
			}
		}
		return fieldNames;
	}

	public static String uppercaseFirst(String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}

	/**
	 * Convert a string into an object of the class it is supposed to come from.
	 * This method is expected to work only for String (quite logical, isn't ?) and classes providing either a constructor using String as an argument,
	 * or a valueOf method having String as an argument. Any other case will miserably fail (ie a NoFromStringConversionExists will be thrown)
	 * @param value input string value
	 * @param type expected output type
	 * @return an object of that type
	 */
	@SuppressWarnings("unchecked")
	public static <Type> Type fromString(String value, Class<Type> type) {
		if(String.class.equals(type)) {
			return (Type) value.toString();
		} else if(type.isPrimitive()) {
			return (Type) fromString(value, objectify(type));
		} else if(URI.class.equals(type)) {
			try {
				return (Type) new URI(value);
			} catch(URISyntaxException e) {
				throw new UnableToBuilddURIException("\""+value+"\" can't be transformed into an URI object", e);
			}
		} else if(Class.class.equals(type)) {
			return (Type) classFromString(value);
		} else {
			/* First check if a constructor exists */
			try {
				Constructor<Type> withString = type.getDeclaredConstructor(String.class);
				return withString.newInstance(value);
			} catch(Exception e) {
				/* This constructor does not seems to exists. Is there any chance a "valueOf" method exists (useful for numbers objects) ? */
				try {
					Method valueOf = type.getDeclaredMethod("valueOf", String.class);
					return (Type) valueOf.invoke(null, value);
				} catch (Exception e1) {
					/* Seems like the reply is no */
					throw new NoFromStringConversionExistsException(type, e, e1);
				}
			}
		}
	}

	/**
	 * Try to load given type class
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <Type> Type classFromString(String value) {
		value = maybeObjectify(value);
		ClassLoader[] used = new ClassLoader[] {value.getClass().getClassLoader(), Utils.class.getClassLoader(), Thread.currentThread().getContextClassLoader()};
		for(ClassLoader c : used) {
			if(c!=null) {
				try {
					return (Type) c.loadClass(value);
				} catch (ClassNotFoundException e) {
					// nothing to do, a better exception will be built later
				}
			}
		}
		throw new UnableToLoadClassException(value, used);
	}

	/**
	 * Similarly to {@link #getUppercasedMap(Method[])}, this method produces a map linking name (in their initial case) to the method object used to call them
	 * @param methods
	 * @return
	 */
	public static Map<String, Method> getNameMap(Method[] methods) {
		Map<String, Method> methodsNames = new HashMap<String, Method>();
		for(Method method : methods) {
			if(!method.getDeclaringClass().equals(Object.class)) {
				String name = method.getName();
				methodsNames.put(name, method);
			}
		}
		return methodsNames;
	}

	/**
	 * Put all classes extended or implemented by this one (including itself) in a collection
	 * @param declaring
	 * @return
	 */
	public static Collection<Class<?>> allClassesOf(Class<?> declaring) {
		Collection<Class<?>> returned = new LinkedList<Class<?>>();
		if(declaring!=null) {
			if(!declaring.equals(Object.class)) {
				returned.add(declaring);
				returned.addAll(allClassesOf(declaring.getSuperclass()));
				for(Class<?> i : declaring.getInterfaces()) {
					returned.addAll(allClassesOf(i));
				}
			}
		}
		return returned;
	}

	/**
	 * Remove from the given list of methods all methods declared in gaedo packages
	 * @param methods
	 * @return
	 */
	public static Method[] removeGaedoInternalMethodsFrom(Method[] methods) {
		String gaedoBasePackage = "com.dooapp.gaedo";
		Collection<Method> filtered = new ArrayList<Method>();
		for(Method m : methods) {
			if(m.getDeclaringClass().isAssignableFrom(Informer.class)) {
				// this is Informer#get(String) method
				if(m.getName().equals("get") && m.getParameterTypes().length==1)
					filtered.add(m);
			} else {
				// method not declared in informer superinterfaces, so it can be added
				filtered.add(m);
			}
		}
		return filtered.toArray(new Method[filtered.size()]);
	}
}

package com.dooapp.gaedo.finders.root;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.logging.Logger;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.FieldInformerAPI;
import com.dooapp.gaedo.finders.FieldProjector;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.patterns.Proxied;
import com.dooapp.gaedo.utils.BasicInvocationHandler;
import com.dooapp.gaedo.utils.CallMethodResolver;
import com.dooapp.gaedo.utils.MethodResolver;
import com.dooapp.gaedo.utils.VirtualMethodCreationException;

/**
 * This invocation handler will transform calls to synthetic informers getters into calls to the {@link #realInformer}'s {@link Informer#get(String)} method.
 *
 * @author Nicolas
 *
 */
public class InformerClassInvocationHandler<InformerType extends Informer<ContainedType>, ContainedType> extends BasicInvocationHandler<InformerType, MethodResolver>
		implements InvocationHandler, Proxied, Validable {
	private static Logger logger = Logger
			.getLogger(InformerClassInvocationHandler.class.getName());

	static final String SYNTHETIC_GETTER_PREFIX = "get";
	/**
	 * Utility method obtaining the field informer associated to a particular method name.
	 * @param <ContainedType>
	 * @param method method
	 * @param methodName
	 * @param realInformer
	 * @return
	 */
	public static <ContainedType> FieldInformer methodNameToFieldInformer(Method method,
			Informer<ContainedType> realInformer) {
		String methodName = method.getName();
		if(methodName.startsWith(SYNTHETIC_GETTER_PREFIX)) {
			String fieldName = methodName.substring(SYNTHETIC_GETTER_PREFIX
					.length());
			if (fieldName.length() > 0) {
				String realFieldName = fieldName.substring(0, 1).toLowerCase();
				if (fieldName.length() > 1)
					realFieldName += fieldName.substring(1);
				try {
					return realInformer.get(realFieldName);
				} catch(NoSuchFieldInHierarchyException e) {
					throw new MethodConstructedOnMissingField(method, realFieldName, e);
				}
			} else {
				throw new EmptyGetDontWorkException(method);
			}
		} else {
			throw new BadPrefixException(method);
		}
	}

	/**
	 * Utility method obtaining the field informer associated to a particular method object (for better error text, use this if you can).
	 * @param <ContainedType>
	 * @param method method
	 * @param methodName
	 * @param realInformer
	 * @return
	 */
	public static <ContainedType> FieldInformer methodNameToFieldInformer(
			String methodName, Informer<ContainedType> realInformer) {
		if(methodName.startsWith(SYNTHETIC_GETTER_PREFIX)) {
			String fieldName = methodName.substring(SYNTHETIC_GETTER_PREFIX
					.length());
			if (fieldName.length() > 0) {
				String realFieldName = fieldName.substring(0, 1).toLowerCase();
				if (fieldName.length() > 1)
					realFieldName += fieldName.substring(1);
				return realInformer.get(realFieldName);
			} else {
				throw new EmptyGetDontWorkException(methodName);
			}
		} else {
			throw new BadPrefixException(methodName);
		}
	}

	/**
	 * Class of contained objects, to have reference fields (like previous
	 * field, I don't really know why it's here)
	 */
	protected final Class<ContainedType> containedType;

	/**
	 * This field is used when trying to project this informer as another class's field (you know, for some method chainings)
	 */
	protected final Class<InformerType> informerClass;

	/**
	 * Real used informer, all calls will be directed to it
	 */
	protected final Informer<ContainedType> realInformer;

	/**
	 * This class used to eager load all methods, but this generates failures in case when a class refers to
	 * itself as member (see Tag test bean for an example of such a configuration).
	 * @param informerClass
	 * @param containedType
	 * @param informer
	 */
	public InformerClassInvocationHandler(Class<InformerType> informerClass,
			Class<ContainedType> containedType, Informer<ContainedType> informer) {
		super(informerClass);
		this.informerClass = informerClass;
		this.containedType = containedType;
		this.realInformer = informer;
	}

	/**
	 * Create resolver by doing the initial job of code transformation
	 */
	@Override
	protected MethodResolver createResolver(Method method) throws VirtualMethodCreationException {
		Class<?> declaringClass = method.getDeclaringClass();
		if(declaringClass.equals(FieldProjector.class)) {
			// Oh my god ! The dread projector method
			return new CallMethodResolver(method) {

				@Override
				protected Object callMethod(Method method, Object[] invokedArgs) throws Throwable{
					return invokeProjectorMethod(method, invokedArgs);
				}
			};
		} else if (declaringClass.equals(FieldInformerAPI.class)) {
			return new CallMethodResolver(method) {
				@Override
				protected Object callMethod(Method method, Object[] invokedArgs) throws Throwable {
					return invokeAPIMethod(method, invokedArgs);
				}
			};
		} else if (declaringClass.equals(Validable.class)) {
			return new CallMethodResolver(method) {
				@Override
				protected Object callMethod(Method method, Object[] invokedArgs) {
					return invokeValidableMethod(method, invokedArgs);
				}
			};
		} else if (declaringClass.equals(Proxied.class)) {
			return new CallMethodResolver(method) {
				@Override
				protected Object callMethod(Method method, Object[] invokedArgs) {
					return invokeProxiedMethod(method, invokedArgs);
				}
			};
		} else if (declaringClass.isAssignableFrom(Informer.class)) {
			return new CallMethodResolver(method) {
				@Override
				protected Object callMethod(Method method, Object[] invokedArgs) {
					return invokeInformerMethod(method, invokedArgs);
				}
			};
		} else  {
			return createSubInformerResolvers(method);
		}


	}

	/**
	 * Invokes a method on this object, which IS validable
	 * @param invokedMethod
	 * @param invokedArgs
	 * @return
	 * @category invoke
	 */
	protected Object invokeValidableMethod(Method invokedMethod, Object[] invokedArgs) {
		try {
			return invokedMethod.invoke(this, invokedArgs);
		} catch (IllegalArgumentException e) {
			throw new InvocationFailedException(e);
		} catch (IllegalAccessException e) {
			throw new InvocationFailedException(e);
		} catch (InvocationTargetException e) {
			if(e.getTargetException() instanceof CrudServiceException)
				throw (CrudServiceException) e.getTargetException();
			throw new InvocationFailedException(e);
		}
	}

	/**
	 * Create method for sub-informer class declarations. Those are the "magical" ones which bind field names in method name to
	 * Informers.
	 * @see #methodNameToFieldInformer(Method, Informer)
	 * @param method input method
	 * @return a method resolving call to an informer
	 */
	private MethodResolver createSubInformerResolvers(Method method) {
		// First thing to check is method startup
		FieldInformer returned = methodNameToFieldInformer(method, realInformer);
		Class<?> returnType = method.getReturnType();
		if (!returnType.isAssignableFrom(returned.getClass())) {
			throw new ReturnTypeMismatchException(method,
					returned.getClass(), returnType);
		}
		return new SubInformerMethodResolver(returned, realInformer);
	}

	public Class<ContainedType> getContainedType() {
		return containedType;
	}

	public Class<InformerType> getInformerClass() {
		return informerClass;
	}

	public Informer<ContainedType> getRealInformer() {
		return realInformer;
	}

	/**
	 * Invoke an informer method on {@link #realInformer}
	 * @category invoke
	 * @param invokedMethod
	 * @param invokedArgs
	 * @return
	 */
	private Object invokeInformerMethod(Method invokedMethod,
			Object[] invokedArgs) {
		try {
			return invokedMethod.invoke(realInformer, invokedArgs);
		} catch (Exception e) {
			throw new InvocationFailedException(e);
		}
	}

	/**
	 * Invoked when user calls a method from the FieldInformerAPI interface on that proxy. It allows us to mimic some behaviours
	 * (typically the {@link FieldInformerAPI#with(Collection)} method will be abused)
	 * @param method
	 * @param invokedArgs
	 * @return
	 * @category invoke
	 * @throws Throwable
	 */
	protected Object invokeAPIMethod(Method method, Object[] invokedArgs) throws Throwable {
		return invokeProjectorMethod(method, invokedArgs);
	}

	/**
	 * A new proxy is created, proxying the object returned by {@link Informer#asField(java.lang.reflect.Field)} with the same interface than the source {@link #realInformer}
	 * @param invokedMethod
	 * @param invokedArgs
	 * @return
	 * @category invoke
	 */
	private Object invokeProjectorMethod(Method invokedMethod,
			Object[] invokedArgs) throws Throwable {
		Informer asField = (Informer) invokedMethod.invoke(realInformer, invokedArgs);
		return Proxy.newProxyInstance(informerClass.getClassLoader(), new Class<?>[] {informerClass},
				new InformerClassInvocationHandler(informerClass, containedType, asField));
	}

	/**
	 * Invoke a method from the proxied interface. In fact, the method is invoked on this object
	 * @param invokedMethod invoked method
	 * @param invokedArgs invoked args
	 * @return
	 * @category invoke
	 */
	private Object invokeProxiedMethod(Method invokedMethod,
			Object[] invokedArgs) {
		try {
			return invokedMethod.invoke(this, invokedArgs);
		} catch (Exception e) {
			throw new InvocationFailedException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sOut = new StringBuilder();
		sOut.append(getClass().getName()).append("(");
		sOut.append("informerClass:").append(getInformerClass().getName());
		sOut.append("; containedType:").append(getContainedType().toString());
		sOut.append("; realInformer:").append(getRealInformer().getClass().getName());
		sOut.append(")");
		return sOut.toString();
	}

	/**
	 * Upon validation, all method resolvers are created, which may fail noisily
	 */
	@Override
	public void validate() {
		createAllMethodResolvers(informerClass);
	}
}

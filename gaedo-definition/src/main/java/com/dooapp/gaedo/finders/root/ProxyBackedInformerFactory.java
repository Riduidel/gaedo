package com.dooapp.gaedo.finders.root;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.patterns.Proxied;

/**
 * This time come the authentical rock'n'roll ! This class will implement
 * {@link Informer} objects from sub-interfaces by redirecting calls to existing
 * {@link Informer#get(String)} method and casting the result to the correct
 * type .
 * 
 * As an example, suppose you have a classic User bean (this is only an example
 * implementation, that I wouldn't be proud letting go out of my javadoc):
 * 
 * <pre>
 * public class User {
 * 		private String login;
 * 		private String password
 * }
 * </pre>
 * 
 * One theorically have to write the full Informer for that bean, which is
 * tedious and absolutely without the slightest interest (read boilerplate code
 * or why can't you generate it).
 * 
 * Using this class, it is possible to create only an informer interface
 * (declaring the methods to use, and gaedo will guess their code for you.
 * 
 * As a consequence, suppose you want your informer to look like
 * 
 * <pre>
 * public interface UserInformer extends Informer&lt;User&gt; {
 * 	public StringInformer getLogin();
 * 
 * 	public StringInformer getPassword();
 * }
 * </pre>
 * 
 * All you have to do is call
 * 
 * <pre>
 * ProxyBackedInformerFactory factory = new ProxyBackedInformerFactory(/ *the informer factory to use* /)
 * UserInformer usable = factory.get(UserInformer.class, User.class);
 * </pre>
 * 
 * And gaedo will do all that boilerplate code for you.
 * 
 * @author Nicolas
 */
public class ProxyBackedInformerFactory extends AbstractInformerFactory
		implements InformerFactory {
	public ProxyBackedInformerFactory(
			ReflectionBackedInformerFactory reflectiveInformerFactory) {
		super(reflectiveInformerFactory);
	}

	/**
	 * Build invocation handler and the rest of the required code for the input
	 * interface
	 * 
	 * @param <InformerType>
	 * @param <ContainedType>
	 * @param informerClass
	 *            expected informer class to implement
	 * @param containedType
	 *            contained object type
	 * @return an invocation handler that will transform calls to correct ones.
	 */
	protected <InformerType extends Informer<ContainedType>, ContainedType> InformerType createDynamicInformer(
			Class<InformerType> informerClass,
			Class<ContainedType> containedType,
			Informer<ContainedType> existingInformer) {
		ClassLoader classLoader = informerClass.getClassLoader();
		Class<?>[] interfaces = new Class<?>[] { informerClass, Proxied.class, Validable.class };
		InformerClassInvocationHandler invocationHandler = new InformerClassInvocationHandler(informerClass, containedType, existingInformer);
		return (InformerType) Proxy.newProxyInstance(
					classLoader, 
					interfaces, 
					invocationHandler);
	}
}

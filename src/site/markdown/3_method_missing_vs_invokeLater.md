In presentation article of gaedo, we explained (near the end) that gaedo can use methods declared in interfaces without even having to implement these methods. How does it works ? 

Well, this is a typical case of the understimated powers of Java dynamic nature. Contents

# The inspirators 

We all saw in Ruby on Rails how [method_missing][1] was put to work for generating [`ActiveRecord`][2] finders, back in the days where RoR was really the trendy topic. Now, RoR hass returned to its status of high productivity web framework. And its ideas have been borrowed wwith styles by other web frameworks, like [Grails][3], as an example. 

We were really astounished by this feature, and really wanted to have it, in any possible fashion, in gaedo. 
 Dynamic proxies 101 

Well, it turns out it was rather simple ... once you know the rules. In the case of gaedo, there are some use case for these dynamic methods :
 
* dynamic finders 
* dynamic informers 

As you already know by having read the linked articles, in both cases, the rules for method content are rather clear. What's more, these rules are associated to already existing code : QueryBuilder in dynamic finders case and Informer in dynamic informer case. As a consequence, in both cases, we can write an alogirthm transforming a method name into some executable code. And that's exactly what we do using the magic of [Java dynamic proxies][4] ! Let me explain it in a few words. In java, when declaring an interface, one have more than one choice to implement it : 

* Create a class that directly implements it 
* Create a proxy that will transform its calls. 

The proxy, contrary to a class that really implements interface methods, simply redirect calls to methods of this interface to an invocation handler. The invocation handler interface is rather simple to implement : 

    public interface InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args)
    	throws Throwable;
    }

The invoke method will be called by java internal mechanism each time a method of the interface is called. As one may note, the invoke method is called with the required parameters to do all that we want : 

* the input proxy object (honestly, I never encnouter a use case where this object was needed. i think its only used when the same invocation handler is used with various proxies, what I do find a bad pattern). 
* the [method object][5], containing all infos about the method being invoked (its name, signature) 
* the actual call parameters 

Using that, do you think it's that hard to transform a method name into a code block for lower level invocation ? 

# Our use cases 

In these use cases, I'll once again reuse the example classes of compilable queries : User and Post. 
## Dynamic informer 

As a simple start example, let's see how the dynamic informers are all handled. A dynamic informer class is used to declare a finder service. This finder service can, under some circumstances as an example when a query is performed) be asked for a dynamic informer instance (by the `FinderCrudService#getInformer()`). in such a case, the `FinderCrudService` usually delegates the call to the `ProxyBackedInformerFactory` class, which will create a proxy for the dynamic informer interface like so : 

    // Creation of this class is out of scope
    ProxyBackedInformerFactory factory =...;
    return proxyInformerFactory.get(UserInformer.class, User.class);

 Let's dive into its code ! In the most interesting (ie when no such proxy already exists), a new one is created by the create method : 

    return (InformerType) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {informerClass}, 
        new InformerClassInvocationHandler(UserInformer.class, User.class, reflectiveInformerFactory.get(containedType)));

 This is the interesting part. To avoid too complex invocation handler code, we choose to create an invocation handler for each Informer sub-interface. This way, the invoke method of the InformerClassInvocationHandler stay rather simple : 

	public Object invoke(Object proxy, Method invokedMethod,
			Object[] invokedArgs) throws Throwable {
		if(invokedMethod.getDeclaringClass().equals(FieldProjector.class)) {
			// Oh my god ! The dread projector method
			return invokeProjectorMethod(invokedMethod, invokedArgs);
		} else if (invokedMethod.getDeclaringClass().isAssignableFrom(Informer.class)) {
			return invokeInformerMethod(invokedMethod, invokedArgs);
		} else  {
			// Ok, now come the real fun. A method declared here should start by
			// get and be followed by a fieldName with first letter uppercased,
			// for it to be translated.
			String methodName = invokedMethod.getName();
			if (methodName.startsWith(SYNTHETIC_GETTER_PREFIX)) {
				return invokeSyntheticGetter(invokedMethod, methodName);
			} else {
				throw new BadPrefixException(invokedMethod);
			}
		}
	}

 According to method name, and method decalring interface, we will execute various code redirections. The must interesting part being obviously `invokeSyntheticGetter`. This method will simply check that method starts with "get" and find the field name following that prefix. 

## Dynamic finders 

Well, I won't detail this example. Interested ones should go take a look at `DynamicFinderHandler` and its associated `MethodResolver` class. Suffice to say these two class perform some interesting tricks using string splitting according to various criterias, return type escalation (to allow findAllBy* methods to return collection, list or even TreeSet), and even method parameters types checking. 
# Advantages and drawbacks 

Since this use of dynamic code has been defined as one of the core features of gaedo, it has been widespread in the code, especially for these finders things. We now consider it a standard tool of gaedo, with its advantages and drawbacks. here they are : 

 Anyway, as (G)Rails are famous frameworks specially due to their dynamic finders, it seems us an interesting tool to integrate in our toolkit.

## Advantages

* Smaller code 
* Ultra-legible find code 
* No more need to reconstruct a collection/list/set from results 
* Reliance upon existing code

## Drawbacks

* Errors appear at runtime 
* Sometimes harder to debug 
* Longer development of new find feature (since MethodResolver need to integrate it in its resolution mechanism)


  [1]: http://www.thirdbit.net/articles/2007/08/01/10-things-you-should-know-about-method_missing/
  [2]: http://weblog.jamisbuck.org/2006/12/1/under-the-hood-activerecord-base-find-part-3
  [3]: http://www.grails.org/GORM+-+Querying
  [4]: http://java.sun.com/j2se/1.5.0/docs/api/java/lang/reflect/Proxy.html
  [5]: http://java.sun.com/j2se/1.5.0/docs/api/java/lang/reflect/Method.html



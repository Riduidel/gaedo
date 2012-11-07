**mandatory disclaimer** We try as far as possible to make sure this article stays up-to-date with existing code. As a consequence, some modification may appear. Fortunatly, as articles are wiki pages, one may access previous versions and page history. 

Well, compilable queries are cool, no problem about it. But the code is ... so big ! No ?

# Dynamic finders goodness 

Don't you find it overkill to have to write

		u = userService.find().matching(new QueryBuilder<UserInformer>() {
 
			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().equalsTo("toto");
			}
		}).getFirst();

 When Grails code allows you to write it as something like

		u = userService.findOneWithLoginEqualsTo("toto");

 Would be cool to have it working ? Well, Why are you asking that ? You have that feature ! Mmh, you think I'm making fun of you, aren't you ? You think that you'll have to write an implementation somewhere ? Well, you're wrong. You only have to follow some rules I'll explain you below, and voila ! It simply works. 

# Interface declaration 

First, you have to declare an interface extending the `DynamicFinder interface for the class you're willing to build dynamic finders on. Taking back the example of compilable queries (`User` and `Post` classes), if I want to make a `DynamicFinder` for `User`, I'll declare the interface the following way :

    public interface DynamicUserService extends DynamicFinder<User, UserInformer>,
    		FinderCrudService<User, UserInformer> {
    	public int countByLoginEqualsTo(String login);
     
    	public User findOneWithLoginEqualsTo(String login);
    }

Don't care for now about method names, I'll explain them later. Once you have this interface, how to use it ? Simple, again, supposing you already have built a back-end service, simply implementing the `FinderCrudService` interface for your user class :

		// Creating the backEnd service is out of this article scope
		FinderCrudService<User, UserInformer> backEnd = (FinderCrudService<User, UserInformer>) TestEnvironmentProvider.create().get(Tag.class);
		// Any implementor of ServiceGenerator interface could be used, but since ServiceGeneratorImpl is our only implementor, let's use it
		ServiceGenerator generator = new ServiceGeneratorImpl();
		DynamicUserService service = (DynamicUserService) generator.generate(
				DynamicUserService.class, backEnd);

Once obtained, one can call the aforementionned methods and have results, it will work, undoubtly (or fail with an exception we hope is clear enough for you to modify your code). But how do you have to write your methods declarations ? 

# Method declaration rules 

A method is made of some parts, and for all of these parts, some convention applies. 
   * Return type may allow in some case dynamic conversion 
   * A method prefix must be defined 
   * Then, a field name must be put with some rules 
   * For this field, an expression must be chosen 
   * Depending upon the query, a combination can be used 

And finally, method parameters must match the various required values for expressions 

## Method prefix 

Prefix are allowed for some of the result fetching modes 

<table>
<tr>
<th>mode</th><th>prefix</th><th>return type</th><th>Notes</th></tr>
<tr><td>count</td><td>countBy</td><td>int, Integer, long, Long</td><td>Any of those types can be accepted, although integer.!MAX_VALUE can be reached without raising exceptions</td></tr>
<tr><td>getAll</td><td>findAllBy</td><td>Any Iterable sub-interface from java.util</td><td>Notice that by using another type than Iterable, Iterable loading is required, which may cause =!OutOfMemoryException= or very long code run, so, use it with care</td></tr>
<tr><td>get(start, end)</td><td>findRangeBy</td><td>Exactly like getAll, any sub-interface of iterable can be used.</td><td>Be careful : when using this query mode, there are two addintionnal parameters that must be put in method signature as the two first parameters. These two parameters must be int or Integer. in any other case, a `MethodBindingException` will be thrown.</td></tr>
<tr><td>getFirst</td><td>findOneWith</td><td>The type used for service definition, or one of its super-classes or super-interfaces</td><td> </td></tr>
</table>

## Query expressions 

These expressions define what we're searching for 

### Field definition 

Each time a field name is put in method declaration, its first letter is uppercased, to comply with method name conventions. As an example User login field will be put in method declaration as Login. Any field of the class declared by service or any of its super-classes or super-interfaces can be used in a dynamic finder method declaration.
 
### Expression 

Once a field name has been put, an expression must be used for this field. The usable expressions are all associated to the  `FieldInformer` used to represent this field in our query API. As an example, for a String field (and login is such a field), the following expressions are usable (those of `ObjectFieldinformer` and `StringFieldInformer`) : 

   * equalsTo(Object other) 
   * contains(String contained) 
   * startsWith(String start) 
   * endsWith(String end) 

Notice the method parameters types ? These types must be (or types that can be assigned to these ones) the ones in method parameters declaration at the position of the field (a more complex example will explain it all later). 

### Combination 

Writing a dynamic finder with one field is cool, but with more than one field, it's really great. For that, one can combine expressions using either Or or And. I expect their meaning to be clear enough. However, their is one restriction. Since a method name can't contain parenthesis, the combination used is always the last one.
 
## Sorting expressions 

These expression define in which order the results will be returned. They are introduced in method name by the text SortBy. 

### Field definition 

The field definition works exactly like for query expressions 
### Ordering 

Two ordering mode are supported : 

   * Ascending 
   * Descending 
### Combination 

only one combination is supported : Then. It helps you memorize that sorting is done one after the other. 
# Examples 

now, some examples to make things clear. 
## Simple one 

The above example can be translated from

	User u = findOneWithLoginEqualsTo("toto");

 to 

		User u = userService.find().matching(new QueryBuilder<UserInformer>() {
 
			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().equalsTo("toto");
			}
		}).getFirst();


## Or example 

To check if login is either "toto" or "tata", one could write 

	User u = findOneWithLoginEqualsToOrLoginEqualsTo("toto", "tata");

 or prefer the longer 

		User u = userService.find().matching(new QueryBuilder<UserInformer>() {
 
			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return Expression.or(
					object.getLogin().equalsTo("toto"),
					object.getLogin().equalsTo("tata"));
			}
		}).getFirst();

## Range example 

To get the list of the 20 first users with logins starting with "l", one can write

	Collection<User> u = findRangeByLoginEqualsTo(0,20, "toto", "tata");

or

		Iterable<User> u = userService.find().matching(new QueryBuilder<UserInformer>() {
 
			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().startsWith("l");
			}
		}).get(0,20);


# What do not works yet 
   * Criterias on collections 
   * Class navigation (like querying to get user which wrote a post titled "Compilable queries, a senseless concept ?". Nope, you won't find me) 
   
But they may work in a far or near future, depending upon your will ;-)

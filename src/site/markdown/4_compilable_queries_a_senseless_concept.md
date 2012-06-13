In presentation post, we shows out maybe too fast most prominent features of gaedo. 

Between all those features, the most distinguishing is the astract query building mechanism.

Query API 
=========

Indeed, when Hibernate allows one to create a query [by example][1], or by using [HQL][2], or even a [hibernate-specific criteria API][3] (which is very performant), we preferred for gaedo to use a query API that is back-end independant (or as far as possible). As a consequence, theorically, the same query can be run against a collection back-end or against a google datastore back-end. We find it rather cool. And the fact that these queries are fully compiled, making all query errors compile-time visible (like, as an example, using a GREATER THAN on a String), is even cooler to us ! 

But how does it works ? 

Suppose you have a application made of `User` and `Post` (like, say, this blog could be stored - it's obviously not the case, but it could) like the ones used by `gaedo-google-datastore` : 

    public class User {
    	@Id
    	public long id;
     
    	private String login;
     
    	public String getLogin() {
    		return login;
    	}
     
    	public void setLogin(String login) {
    		this.login = login;
    	}
     
    	public String password;
     
    	public Collection<Post> posts = new LinkedList<Post>();
    }

(don't focus on the public/private nightmare, it's some test code allowing us to check our hypothesis)

    public class Post {
    	@Id
    	long id;
     
    	public String text;
    	public float note;
    	public boolean test;
     
    	public User author;
     
    	public Map<String, String> annotations = new TreeMap<String, String>();
    }

What we want is the ability to write queries like this (yes, it's really the kind of code that designed our API) :

		u = userService.find().matching(new QueryBuilder<UserInformer>() {
 
			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().equalsTo("toto");
			}
		}).getFirst();

So, how does it works ? 

Query building blocks
=====================

A query is made of some basic building blocks : 

* a matching expression 
* a return type 

Matching expression 
-------------------

For building the matching expression in a truly "abstract" fashion, we need to build a parsable syntactical tree. As a consequence, we can't write

			public QueryExpression createMatchingExpression(User object) {
				// For the record, this code is just WRONG, don't copy/paste it without reading !!!
				return object.getLogin().equalsTo("toto");
			}

 Without relying upon heavy code generation (like, as an example, creating dynamically, through [cglib][4], a subclass of `User` in which calls will be used to generate a syntactical tree). As a consequence, we made up the concept of `FieldInformer`/`Informer`. 

FieldInformer 
-------------

A `FieldInformer` describes a Field and memorizes expression generated from this field. As an example, the `FieldInformer` root interface is as follows

    public interface FieldInformer {
    	/**
    	 * Check if field is equals to value
    	 * 
    	 * @param value
    	 *            checked value
    	 * @return a query expression checking. A new one will be created each time.
    	 */
    	public QueryExpression equalsTo(Object value);
    }

Notice this is really a base interface. Implementors of this interface are left free (and are greatly encouraged) to add others QueryExpression building methods. Also notice that a QueryExpression is really what is seems : an expression, destined to be added to a query. As of now (february 2010), implementors of FieldInformer are : 

   * ObjectFieldInformer 
      * StringFieldInformer 
      * ComparableFieldInformer<ComparableType> 
         * DateFieldInformer 
         * DoubleFieldInformer 
         * LongFieldInformer 
         * BigDecimalFieldInformer 
   * The Informer interface 
      * BigIntegerFieldInformer 
 
Informer 
--------

The `Informer` interface is an extension to the `FieldInformer`, allowing one to compare an object to another using equalsTo `QueryExpression`, but also to access one object's fields, using its get method :

    public interface Informer<Informed extends Object> extends FieldInformer, FieldProjector<Informed> {
    	/**
    	 * get field informer for one of the fields of current object
    	 * @param string
    	 * @return
    	 */
    	FieldInformer get(String string);
    }

 This way, all fields can be accessed in the standardmost way : as simple FieldInformer. 

Dynamic Informers 
-----------------

That's rather boring ! Suppose I have, like in User, a login stored as String. To provide completion in login window (maybe not the most interesting use case), I would like to write something like 

			@Override
			public QueryExpression createMatchingExpression(Informer<User> object) {
				return object.get("login").startsWith("toto");
			}

 which is totally possible, using a rather subtile trick. Indeed, one may have note this expression building uses `Informer<User>` instead of `UserInformer`. Well, here is the trick. Since this code will be rather frequent in your code, you don't have to write it ! Using a trick we'll explain later (suffice to say it involves [dynamic proxies][5], and as a consequence is runtime-resolved), you only have to declare the following interface, and gaedo will do the whole work !

    public interface UserInformer extends Informer<User> {
    	public StringFieldInformer getLogin();
    	public StringFieldInformer getPassword();
    }

 using that interface, we finally can express our query as we want to :

			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().equalsTo("toto");
			}

 Which is easier, no ? once our query is built, we want to get back some results. 

Result fetching modes 
---------------------

Like in ay data storage mechanism, there are some way to retrieve results, which are all defined using an interface (you'll soon start to get used to the multiplicity of interfaces of gaedo ;-)) :

    public interface QueryStatement<DataType> {
    	DataType getFirst();
    	Iterable<DataType> getAll();
    	Iterable<DataType> get(int start, int end);
    	int count();
    }

This way, most common operations are possible. 

Sorting : the missing fragment 
==============================

yes, for now, custom sorting of results is not supported. That is a big miss (but honestly, since not very high on our priorities, it has not yet been implemented by pure lack of time). Just wait until issue #24 is marked as fixed ;-)


  [1]: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/querycriteria.html#querycriteria-examples
  [2]: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/queryhql.html
  [3]: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/querycriteria.html
  [4]: http://cglib.sourceforge.net/
  [5]: http://java.sun.com/j2se/1.5.0/docs/api/java/lang/reflect/Proxy.html


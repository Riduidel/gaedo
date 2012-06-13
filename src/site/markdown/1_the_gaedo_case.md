So, you came on this site and want to know what the heck is gaedo ? As any acronymic name, unfolding it may not be of real interest : Google App Engine Data Objects. Well, pretty obvious, isn't it ? in fact, the point of gaedo is precisely to avoi what some have already advocated as an anti-pattern. I mean, when using a DAO, you start creating your POJOs, to immediatly duplicate them in so-called data access objects, making your code full of copy methods. By using the architecture provided by gaedo, you immediatly get rid of all that fuss. Indeed, for a POJO, using gaedo, you have to define two things 

* a FinderCrudService interface that will define the available operations 
* an Informer subinterface, that will define what fields will be usable for queries (we will go back longer on that second). 

As you may have noted from code, the FinderCrudService interface is quite simple 

    /* This is a "compressed" view of the service interface */
    public interface FinderCrudService<DataType, InformerType extends Informer<DataType>> extends AbstractCrudService<DataType> {
    	/**
    	 * Create a new object of the given type and returns it
    	 * 
    	 * @param toCreate
    	 *            object to save
    	 * @return returned object
    	 */
    	public DataType create(DataType toCreate);
     
    	/**
    	 * Delete an object of the given type
    	 * 
    	 * @param toDelete
    	 *            object to delete
    	 */
    	public void delete(DataType toDelete);
     
    	/**
    	 * Update an object of the given type
    	 * 
    	 * @param toUpdate
    	 *            object to update
    	 * @return updated object
    	 */
    	public DataType update(DataType toUpdate);
     
    	/**
    	 * Creates a finder for the given datatype. This finder is the delegate that
    	 * will perform the various queries kind
    	 * 
    	 * @return
    	 */
    	public Finder<DataType, InformerType> find();
     
    	/**
    	 * Get class 
    	 * @return
    	 */
    	public Class<?> getContainedClass();
     
    	/**
    	 * Get the informer associated to this data type
    	 * @return an object implementing the interface defined by InformerType.
    	 */
    	public InformerType getInformer();
    }

In all those methods, only two are useed for "internal" purpose (the two lasts) and should not interest you. The others are quite more explicit, especially the create, update and delete> ones. 

In fact, the only that is a little more complicated is find, which reclaims a little explanation. Since i'm a little lazy, the clearest explanation is an obvious example, like one of our tests (`CollectionBackedTagFinderServiceTest`) : 

		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {
 
					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.get("text").equalsTo(A);
					}
				}).getAll();

One can see that, here, by calling the find method, we create a Finder object. By calling this Finder's matching method, we in turn create a QueryStament that has the the getAll method that finally gets called. The most tricky part being the QueryBuilder we use to create a QueryExpression that will in turn be used (internally) by the getAll method. This code is extremely powerful, but rather complicated. And there is one big drawback : the get method is not typesafe : we could call it with any text parameter, it would get called without any problem, and you'll have trouble finding what gone wrong. As a consequence, we decided to let you sub-interface Informer, without bothering you with the associated implementation. As a consequence, cosnidering the Tag class used in this example, there is a TagInformer you could write as such 

    public interface TagInformer extends Informer<Tag> {
    	public StringFieldInformer getText();
    }

 The StringFieldInformer is a specific class allowing us to write complex predicates based upon String. You're encouraged to go take a look at its associated documentation. Anyway, using it, previous code could be rewritten as such : 

		Iterable<Tag> values = tagService.find().matching(
				new QueryBuilder<TagInformer>() {
 
					public QueryExpression createMatchingExpression(
							TagInformer object) {
						return object.getText().equalsTo(A);
					}
				}).getAll();

 Better typing, no ? This typing is important to us, since it allows completion, error highlighting at compile time, and so on. 


but there will be even better ! A service can also use previously mentionned dynamic finders. A dynamic finder interface is an extension to FinderCrudService where tyou only have to declare method (without even implementing them). By using them, previous code could be rewritten as such : 

		Iterable<Tag> values = tagService.findAllByTextEqualsTo(A);

 Far better, isn't it ? And all this should work (in a near future) with 

* GAE 
* collections backed by space4j or prevayler 
* even Hibernate ! (provided someone has enough courage to do the backend code). 

All without any modification in client code (even queries could stay unchanged), excepted annotations sometimes required by backend (GAE, JDO, JPA and Hibernate are great consumers of those).

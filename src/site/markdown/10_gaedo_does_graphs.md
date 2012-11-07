You now that gaedo does GAE persistence. 

You now that gaedo also does prevalence persistence. 

But did you know that gaedo does graph-backed persistence ? 

Well, that's kind of normal, considering it does it only since the 0.2.0 release. 

Relying upon the excellent [Tinkerpop blueprints library][1], we wrote adapted implementations that can be used to store/load/query beans in a graph DB, be it Neo4j, OrientDB, Tinkergraph, and all others (im|out)plementations of Blueprints IndexableGraph. 

We must confess work on this is not yet over, as we somewhat want our persisted graph to be RDF-consistent (that's to say have all elements identifiable using URIs). 

Nevertheless, this implementation is considered usable (and in fact already used). 

But how do one use it ? 

well, as usual, the tests are the answers. Take a look, as an example, to [GraphBackedPostFinderServiceTest][2]. The @Before makes quite clear (to my mind) what one need to have a working graph-backked finder service : 


         repository = new SimpleServiceRepository();
         PropertyProvider provider = new FieldBackedPropertyProvider();
         CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
         locator.add(new BasicFieldInformerLocator());
         locator.add(new ServiceBackedFieldLocator(repository));
         ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
                 locator, provider);
         InformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
                 reflectiveFactory);

         graph = graphProvider.get();
         // Now add some services
         repository.add(new BluePrintsBackedFinderService(Tag.class, TagInformer.class, proxyInformerFactory, repository, provider, graph));
         repository.add(new BluePrintsBackedFinderService(Post.class, PostInformer.class, proxyInformerFactory, repository, provider, graph));
         repository.add(new BluePrintsBackedFinderService(User.class, UserInformer.class, proxyInformerFactory, repository, provider, graph));

         tagService = repository.get(Tag.class);
         postService = repository.get(Post.class);
         userService = repository.get(User.class);

As usual, one need way to construct properties out of raw objects, then a persistent graph (obtained here throught the graphProvider.get() method which i'll explain later), a service repository and some service implementations. And that's all one need ! 

What will this create ? Well, you remember that previous blog post about bouncing query and the schema attached to it ? In fact, this schema was a screenshot of neoclipse (the Eclipse declinaison for neo4j visual exploration). 

Finally, I would like to thank the blueprints ML, and even more particularly the people at neo4j. Indeed, when I did my first test (GraphBackedTagFinderServiceTest), I noticed strange performance patterns. As a consequence, [I mailed the blueprints ML][3] and had a very interesting mail discussion which Peter and Michael of Neo4j. it revealed the following point : 

* On my initial test performance of Neo4j was mainly affected by its startup time, which is indeed longer than both Tinkergraph and OrientDB one, mainly due to its enterprise-grade nature. 
* Those tests are in fact not really representative of long-term use, as I always stop my graph between each test, which is obviously not good. 
* More intensive performance test reveals in fact that node writing with auto-transaction mode lasts no longer than 2-5 ms per vertex write, which is perfectly supportable. 

With all that said there is only one question remaining : what gaedo module contains all thise greatness ? 


     <dependency>
       <groupId>com.dooapp</groupId>
       <artifactId>gaedo-blueprints</artifactId>
       <version>0.2.0</version>
     </dependency>


  [1]: https://github.com/tinkerpop/blueprints/wiki/
  [2]: https://github.com/Riduidel/gaedo/blob/master/gaedo-blueprints/src/test/java/com/dooapp/gaedo/GraphBackedPostFinderServiceTest.java
  [3]: http://groups.google.com/group/gremlin-users/browse_thread/thread/6c98859b75498f15
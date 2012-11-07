Since its beginning, object oriented programming suffered from [impedance mismatch][1] with databases. Google app engine is not an exception. Althgough [datastore][2] provides the ability to store any kind of data without bullying about missing columns, storing an obejct containing a collection of others obejct in a sane fashion reveals to be a rather complicated task.

# An example #

Take as an example the following pair of classes 

    public class User {
	    @Id private long id;
	    private String login;
	    private Collection<Post> posts = new LinkedList<Post>();
	}
     
    public class Post {
	    @Id private long id;
	    private String text;
	    private User author;
	}

There are more than one mapping strategies possible for this collection. 

# Mapping of simple fields #

But before, let me explain shortly how gaedo maps a Java object to an entity. This is rather simple : the field annotated with `@Id` (which must be a long) is used as a placeholder for object's Key (which kind is associated to object class in service and name is associated to real class), and other fields are associated to entity properties. As an example, the Post class is associated to an entity which looks like

<table border="1">
<tr><th>Key</th><th>Post.text</th><th>Post.author</th></tr>
<tr><td>Post={id}</td><td>{text}</td><td>User={author.id}</td></tr>
</table>


As one may already notice, a domain object is always replaced, when used as field from another object or in a collection, by its key. This allow us to escape from the dreaded issue of JPA/JDP, which requires user to put GAE keys in its model when trying to establish relations between objects. 

However, when it comes to collections, the issue is a little more complex. 
# Mapping collection with dynamic properties #

First solution we used was to create properties for each entries. As an example, if User named toto has written posts with texts "A" and "B" we would have the following entities in datastore (for the sake of this example, I removed the user from the Post, to well emphasize the search issue). 

The two posts
<table border="1">
<tr><th>Key</th><th>Post.text</th></tr>
<tr><td>Post=1</td><td>A</td></tr>
<tr><td>Post=2</td><td>B</td></tr>
</table>


And the User
<table border="1">
<tr><th>Key</th><th>User.login</th><th>User.posts.count</th><th>User.posts.0</th><th>User.posts.1</th></tr>
<tr><td>User=1</td><td>toto</td><td>2</td><td>Post=1</td><td>Post=2</td></tr>
</table>


This was a simple storage solution, which has one HUGE drawback : how can I find, as an example, the author of Post 1 ? Take a look at [GQL][3], and you'll find that a query requires a property name. So, do we have to look in column named posts.0 ? or in posts.1 ? or in both ? And how will we handle the case of a user who wrote 3 posts ? Clearly, this doesn't scale. As a consequence, we preferred (after having code this alpha-level prototype) to use the sub-entity mechanism. 

# Mapping collections with subentities #

Indeed, google datastore allows keys to be hierarchical (see `Keyfactory.createKey(Key parent, java.lang.String kind, long id)`). As a consequence, we will create a sub-entity group for each collection of the object, and our example will become (with the Post pair unchanged)
<table border="1">
<tr><th>Key</th><th>User.login</th></tr>
<tr><td>User=1</td><td>toto</td></tr>
<tr><td>Key</td><td>User.post.value</td></tr>
<tr><td>User=1/posts.0</td><td>Post=1</td></tr>
<tr><td>User=1/posts.1</td><td>Post=2</td></tr>
</table>


Using this strategy, will simply consists in querying upon User.post.value property name. obviously, it increases complexity of data structure, since collections are now stored outside of objects. however, it seems to me more clear, and has the added benefit of allowing easy storage of maps, what previous model didn't allowed. 


  [1]: http://en.wikipedia.org/wiki/Object-relational_impedance_mismatch
  [2]: http://code.google.com/intl/fr-FR/appengine/docs/java/javadoc/com/google/appengine/api/datastore/DatastoreService.html
  [3]: http://code.google.com/intl/fr-FR/appengine/docs/python/datastore/gqlreference.html
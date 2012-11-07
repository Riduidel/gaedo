This document focuses on how queries are run on a graph managed by gaedo-blueprints. If one wants to have infos on specific features regarding graph queries, this is the right document. However, if one wants infos regarding how to write a query, regardless of the underlyin storage mechanism, or how dynamic finders are mapped on real `QueryBuilder`, general documentation of gaedo is better suited.

# How is a query performed on graph - a broad view #

To make things clear, explanations here are given in the best case, that's to say when `StrategyType.beanBased` is used. If the previous sentence has no meaning to you, it probably mean you're in that case, as the oppsite strategy (`StrategyType.graphBased`) is the non-default and should be selected by developper.

To make things simples, execution of a query on graph is quite simple and can be splitted in some easy steps

 1. Transform QueryExpression into a collection of VertexTest
 1. Choose some "query roots" from that collection of vertex and back-navigate to vertex corresponding to objects
 1. Perform all VertexTest on those vertices and, if they match all, add them to results iterable
 1. Browse results iterable and load objects on the fly

Now, let's detail these elements on some sample queries.
For all those example queries, we will suppose (like in `GraphPostFinderServiceTest`), we have a 

	// a standard IndexableGraphBackedFinderService with StrategyType.beanBased
	FinderCrudService<Post, PostInformer> service;

which will be used to perform those sample queries

# Find posts having a value #

## Find posts having a given literal value #

First, let's start by a very simple example

	service.find().matching(new QueryBuilder() {
		public QueryExpression createMatchingExpression(PostInformer informer) {
			return informer.getNote().equalsTo(2.0f);
		}
	}).getAll()

The goal of this query is quite simple (and it could also be expressed using this dynamic finder : `findAllByNoteEqualsTo(2.0f)` which has the known advantage of readability), so let's detail this behaviour.

First, `QueryExpression` is visited by a `BluePrintsQueryBuilder` which will construct a `GraphExecutableQuery` using the predicates given by the `QueryExpression`. As the GraphExecutableQuery has a nice toString(), let's use it to see how this query translates at the blueprints level : 

	com.dooapp.gaedo.blueprints.queries.executable.OptimizedGraphExecutableQuery [
		test= AndVertexTest 
			note EqualsTo 2.0
			classes CollectionContains class com.dooapp.gaedo.test.beans.Post, 
	sort=com.dooapp.gaedo.finders.sort.SortingExpressionImpl@47570945]

Well, the sorting expression is useless here (as you may now, this version not yet implements sorting in graph). Anyway, the test is quite clear. let me repeat it : 

	test= AndVertexTest 
		note EqualsTo 2.0
		classes CollectionContains class com.dooapp.gaedo.test.beans.Post, 

This is quite clear : we look for objects having an edge named `note` having as value `2.0` and which `classes` collection contains `com.dooapp.gaedo.test.beans.Post` (a graphw ay to say target node can be casted to an instance of `Post`, no ?).

The graph can be big. **Really** big, I mean, so we clearly won't browse all vertices looking for the ones we're searching. Hopefully, to avoid that (as the picture upper shows), properties are represented as edges. So we're quite sure the results are linked to both the 2.0f literal and to the `com.dooapp.gaedo.test.beans.Post` literal. The ensemblist method would be here to intersect both sets. Unfortunatly, gaedo-blueprints does not yet provide such a feature. To emulate it, in a way, we check which of the set is the smallest, then test each of its elements for matching.

This way we obtain a list of vertices that will be loaded on iteration.

# Find posts having a given object value #

Now how to get posts written by a given author ? Suppose I have a `User` object (named `user`) obtained from a providential `userService`. To get all posts written by this author, it's as simple as 

	service.find().matching(new QueryBuilder() {
		public QueryExpression createMatchingExpression(PostInformer informer) {
			return informer.getAuthor().equalsTo(author);
		}
	}).getAll()

Well, I give the dynamic query to you ...

Anyway, it translates as a GraphExecutableQuery which test is

	AndVertexTest 
		author EqualsTo com.dooapp.gaedo.test.beans.User@b4deeb1c
		classes CollectionContains class com.dooapp.gaedo.test.beans.Post

How is the `author` translated into a vertex you guess ? Well, the `userService` does the job and gives the query a root vertex to count edges on.
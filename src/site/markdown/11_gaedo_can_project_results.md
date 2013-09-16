After long usage of gaedo, it appears to us that something was severly lacking. Something that could be expressed, using classical SQL idiom, in terms of what is selected. Indeed, there was absolutely no way to return a collection of string when perforing queries on objects : we should first return objects, then extract the meaningful strings.

Obviously, it was a pain, since it implies loading tons of objects for no purpose.

And hopefully, this is now over, as gaedo has a way to project results, aptly called ... projections.

# Project me that movie title

The feature is rather simple and is in fact totally integrationed into gaedo classicial search DSL. Let's see an example (extracted from `TestFor62_AKA_Projection`) :

		Iterable<String> result = getUserService().find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer informer) {
				return informer.getLogin().startsWith("u");
			}
		}).projectOn(new ProjectionBuilder<String, User, UserInformer>() {
			public String project(UserInformer informer, ValueFetcher fetcher) {
				return fetcher.getValue(informer.getLogin());
			}
		}).getAll();

This example is rather trivial, as we have only one user defined in our user service. Nevertheless, it clearly exposes, in the `projectOn` method, how the result is transformed from a `User` object (that will *not* be loaded into memory) into a String.

Notice it could totally be transformed into any other object, instead of a simple `String`.

# Concepts

The projectOn method takes as argument a `ProjectionBuilder`, which implements a simple method : `ValueType project(InformerType, ValueFetcher)`. 
This method will be called each time a result which match the criterias expressed in query builder. 
When called, obtaining a given property will require calling the value fetcher which, using proper generics types, will always return the value of the correct type in the given context.

In our example, this mean `fetcher.getValue(informer.getLogin())` returns, by the grace of generics types, a String. 
Obviously, calling `fetcher.getValue(informer.getAbout())` would return a `Post` object. All that **without** loading the initial `User` object. This could be a great memory saver ...

Oh, and if you want to know how this goes with "normal" mode where the object is loaded, then go check the `NoopProjectionBuilder` ...

# Limitations ?

Well, for now, the limitations are quite implementation ones : this work only with the following

* gaedo-collections
* gaedo-blueprints

And unfortunatly not with gaedo-google-datastore, as I've quite lost contact with that particular codebase. Obviously, any volunteer could add this implementation :-)

Oh, there is an other : it's not possible to use that code with dynamic finders. If you have any diea regarding that, again, feel free to submit an issue or a pull request.
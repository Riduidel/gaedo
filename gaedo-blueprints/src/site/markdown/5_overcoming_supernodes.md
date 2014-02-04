## Supernodes, a tale from the trenches ##

I could take long paragraphs to explain why, and how, supernodes are an expression of failure, but I found out that authors of Titan Graph explain that far better than me, so I'll direct you to this excellent article : [A solution to the supernode problem](http://thinkaurelius.com/2012/10/25/a-solution-to-the-supernode-problem/).

How are they a problem with gaedo ?

Well ... remember how gaedo team was proud of having only one instance vertex for each literal ? This means that each object using the integer value "4" or the String "bob" will refer in fact to the same vertex. This is conceptually good. Unfortunatly, due to the way graph engines are defined, this lead to increasing transaction collision as one vertex becomes a supernode. Indeed, to add or remvoe an edge from a supernode, one has to obtain a DB lock over the section containing informations about that vertex (will, it's not the case for all graph engines, but at least it is for neo4j). As a consequence, making each literal value a supernode, and the classes vertices also supernodes, introduces far too many of these beasts.

So, whaty could a solution be ?

## Literal as graph properties ##

Chosen solution was exactly what was criticized in article ["An introduction to gaedo graph storage"](1_gaedo_graph_storage.html), precisely in this part :

> We could imagine storing each of the non-static (and non-transient, of course) fields of that class as one of Tag associated vertex properties. Something like `text` could do the trick, no ? This simple solution would lead to that graph :
> 
> ![A graph where properties are stored as vertex properties](images/gaedo_graph_storage_1_graph_with_properties.jpg)
> 
> Well, it would immediatly raise some issues :
> 
>  1. What if two `Tag` share the same text ?
>  2. What if `Tag` class had a subclass containing an other `text` property.

Hopefully, gaedo team had to answer those questions without even thinking about using properties instead of edges/vertices. That's why chosen edge-full format uses full class name as prefix : the `text` property of the `Tag` class is always named `com.dooapp.gaedo.test.beans.Tag:text`. A long name, for sure, but also a unique one.

# A literal edge-less graph #

So here is current state of graph data storage with gaedo

* managed objects and map values are stored as independant vertices
* literal values, be them in collection or directly in monovaluated properties, are stored as vertices properties.

So, considering canonical example of a Tag object storing the "a text" value, it now looks like this

Well, sorry, but I can't make Neoclipse work with my test graph. Just assume that each literal property is stored as a Vertex property, like this text block shows

	{graph id=com.dooapp.gaedo.test.beans.Tag:com.dooapp.gaedo.test.beans.Tag:1
	com.dooapp.gaedo.test.beans.Tag:text=tag text
	com.dooapp.gaedo.test.beans.base.Identified:id=1
	java.lang.Object:classes.size=4
	java.lang.Object:classes:0=com.dooapp.gaedo.test.beans.Tag
	java.lang.Object:classes:1=com.dooapp.gaedo.test.beans.base.Identified
	java.lang.Object:classes:2=java.io.Serializable
	java.lang.Object:classes:3=java.io.Serializable
	java.lang.Object:classes:com.dooapp.gaedo.test.beans.Tag=true
	java.lang.Object:classes:com.dooapp.gaedo.test.beans.base.Identified=true
	java.lang.Object:classes:java.io.Serializable=true
	java.lang.Object:type=com.dooapp.gaedo.test.beans.Tag
	kind=uri
	value=com.dooapp.gaedo.test.beans.Tag:1}

(yup, all that is a single vertex). Obviously, links to other vertices are expressed as edges, as usual. Now let's consider some new funny things.

* Each collection property now has an integer property "size", which could allow us in the future to query collections for size. Cool ? Or not ?
* Each collection element is present in two properties : one direct indicating index of element in collection, and one reverse indicating if element is in collection. This could have been only indicated in index, for sure, but I prefer to have the index content be reflected directly in graph.
Migrating gaedo to Blueprints 2.*
=================================

Yeah, a new version of Blueprints has been released some time ago. And, since [it introduces some changes](https://github.com/tinkerpop/blueprints/wiki/The-Major-Differences-Between-Blueprints-1.x-and-2.x), gaedo also had to document the changes it implied in it.

## What it implies for gaedo ##

First signifiant change is that, as indices are no more automatically created, they are created at service startup. More specifically, each time an `IndexableGraphBackedFinderService` is created, it check if the needed indices exist.

## What it implies for you ##
Have you noticed the lingo about neo4jGraph [`setCheckElementsInTransaction`](http://www.tinkerpop.com/docs/javadocs/blueprints/2.3.0/com/tinkerpop/blueprints/impls/neo4j/Neo4jGraph.html#setCheckElementsInTransaction(boolean)) ? No ? Well, I urgently advise you to take a look at this. Indeed, gaedo test suite has revealed it **requires** this flag to be set. In fact, when not set, the `GraphPostFinderServiceTest#ensureDeleteCascadesWell()` test not only fails, but also crash during execution, which is not good. Also, notice gaedo doesn't hard check if that flag is set (because it would require gaedo havign the blueprints-neo4j module as a compile dependency, which we absolutely don't want.

So, in any case you have cascade failures, or `java.lang.IllegalStateException: Node[*] has been deleted in this tx` well ... make sure this flag is set.
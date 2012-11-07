So, now you know dynamic finders are that cool, you'll undoubtly start using them. And, sooner or later, you'll write either a dynamic finder, or use a base query, that is too complex to be easily understood. 

Then, `gaedo-graphviz` will come to help you. 

Let me explain. By listening on executing queries, `gaedo-graphviz` generates, in a log file, dot files linked to queries. Each dot file can then be used to generate a graph (like the file associated to this post) using the dreadded [graphviz][1]. 

But how does it works ? Well for pure `gae-datastore`, it is quite simple : simlpy adds the to the ServiceRepository like this 

    repository.addPropertyChangeListener(QueryStatement.STATE_PROPERTY, new DotGeneratorGrapherChangeListener());

And that's all ! 


Then, the next time a query will be executed, a log entry will be written in "graphviz" log handler, allowing one to transform it into a graph.


  [1]: http://www.graphviz.org/

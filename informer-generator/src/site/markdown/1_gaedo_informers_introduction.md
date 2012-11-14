## Foreword ##

This document only gives a short view of what is informer generator.

# A project without a generator isn't that worth #

When development of gaedo started, it appeared as an evidence writing by hand an `Informer` was really not worth the effort : you only have to duplicate fields you find interesting in an interface that return a `FieldInformer` of a type corresponding to the field type. It was a clear candidate for [generative programming][1].

As a consequence I tried at first to use the excellent [spoon][2] source code analysis system. But that integration revealed to be a little too complicated for me.

So I instead used the not less excellent [javaparser][3] library to scan source code for candidate beans and to generate associated informers, which led to current implementation.

# And how not to integrate a generator in maven #

Yes, how not ? After all, to me, maven is now the de-facto build tooling standard : I'm quite sure if I write a MOJO it can be used in gradle, ant, ...

As a consequence, this project was thought from the bottom up as a maven plugin, which eases some things (finding the sources folder, exposing a log), but do not simplify other ones (like testing).


  [1]: http://en.wikipedia.org/wiki/Automatic_programming
  [2]: http://spoon.gforge.inria.fr/
  [3]: https://code.google.com/p/javaparser/
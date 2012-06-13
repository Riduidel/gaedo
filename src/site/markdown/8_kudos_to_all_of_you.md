Kudos to all of you, our beloved concurrents in the easy-to-use, finder-based, dao-patternized, persistance layers or frameworks. So far, we know some of you :
 
   * [Fonzie][1], which has an uber-nice name, and does his tricks thanks to AOP (besides being written by [the author][2] of the very well known french maven book). Its doc is only in french, and it is limted to JPA, however, it seems to be quite nice. 
   * [JaQue][3] is quite different in the sense it mimics Microsoft LinQ on the Java platform. 
   * [simpleDS][4] provides an abstraction over datastore, with simple to write queries, albeit without using dynamic finders, but rather a very legible API (that we hope to introduce later). 
   * [twig-persist][5]'s goal is to make the datastore typesafe, but it does not go that far in that direction. 
   * [warp-persist][6] is very interesting in the sense it allows mapping with both Hibernate and JPA. Besides, it seems to be the only one to provide Transactions (what gaedo do not for now). 
   * [hibernate-generic-dao][7] provides an interesting point of view on the querying subject, with all its dreaded Hibernate magick. 

So far, we only identified those concurrents. Feel free to send us infos on your project, if you feel we do the same job. We, at gaedo, only consider concurrency as a motivation, not as a way to begin a flamewar.


  [1]: http://code.google.com/p/loof/wiki/Fonzie
  [2]: http://blog.loof.fr/
  [3]: http://code.google.com/p/jaque/
  [4]: http://code.google.com/p/simpleds/
  [5]: http://code.google.com/p/twig-persist/
  [6]: http://code.google.com/p/warp-persist/
  [7]: http://code.google.com/p/hibernate-generic-dao/
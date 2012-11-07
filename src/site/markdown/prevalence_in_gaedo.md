A long time ago, I was quite fond of prevalence libraries. There are mostly two of them : [space4j][1] and [prevayler][2]. Both have quite the same feature set, always implementing in less than ten classes, and less than 100 KB. in both case, there is a collection of objects in memory that is accessed in a synchronized fashion, and for which each operation is performed by an object. Those operation objects are all [Serializable][3], and so are the data obejcts stored inside. As a consequence, it is perfectly thinkable to have this datastore regularly persisted to disk, and all operations journaled using serialization. And that's what they did. 

I was quite pleased with them, and wanted to see how I could have such behaviour in gaedo. 

So I've polished my [lincolnian axe][4] and wrote a prevalence layer for gaedo. 

This prevalence layer is made of two distinct parts : 
 a pure prevalence library, using only gaedo exceptions hierarchy, consisting of various Commands, an ExecutionSpace, a StorageSpace, and a SpacePersister. 
 And a gaedo prevalence service, relying upon the prevalence layer and gaedo collections (of which I never spoke ?) to provide to you a persisted, collection backed, complete FinderCrudService. 

Using this service is as simple as usual, like the unit test may shows you (particularly consider the create method, which initializes both the ExecutionSpace and the FinderCrudService). 

So, let me just the time to release the code, and it will be in a maven repository near you.


  [1]: http://www.space4j.org/
  [2]: http://www.prevayler.org/
  [3]: http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/java/io/Serializable.html
  [4]: http://www.brainyquote.com/quotes/quotes/a/abrahamlin109275.html
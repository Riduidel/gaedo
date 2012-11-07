Interested by gaedo ? *cool* 

Wondering how you could integrate it in your code ? 

Well, we, the gaedo developers, are big maven users. As a consequence, gaedo is a maven project split in various modules.
 
Include in a maven project
-------------------------- 

You can include the artifacts you want. Usually, you'll have to include two artefacts : 
 one for the servcie implementation you choose (as an example, `gaedo-google-datastore`) 
 one for your IoC container (as an example, `gaedo-tapestry`) 

All other dependencies are pulled by these two artifacts. So, as an example, if you simply put
 
	<dependency>
		<groupId>com.dooapp</groupId>
		<artifactId>gaedo-google-datastore</artifactId>
		<version>0.2.19</version>
	</dependency>
	<dependency>
		<groupId>com.dooapp</groupId>
		<artifactId>gaedo-tapestry</artifactId>
		<version>0.2.19</version>
	</dependency>

in you dependencies, all required gaedo modules will come. 

As a general policy, we'll always try to release all gaedo modules together. As a consequence, we greatly encourage you to use the same version number everywhere.
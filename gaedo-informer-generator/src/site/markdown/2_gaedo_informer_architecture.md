This document tries to expose clearly how is structured a generated `Informer` using as example the `gaedo-test-beans` ones.

# how to generate #

So, the first point to understand is that informer generation can be transparent to you, the developer, provided you define correctly the goal in your maven pom. In our gaedo-test-beans, we use the following declaration :

		<plugins>
			<plugin>
				<groupId>com.dooapp</groupId>
				<artifactId>gaedo-informer-generator</artifactId>
				<version>${project.version}</version>
				<configuration>
					<requiredInterfaces>
						<!-- Any bean having that interface defined will have its informer 
							generated -->
						<requiredInterface>java.io.Serializable</requiredInterface>
					</requiredInterfaces>
					<propertiesExcludes>
						<!-- Do not inform the message interface from user class -->
						<propertiesExcludes>about</propertiesExcludes>
					</propertiesExcludes>
				</configuration>
				<executions>
					<execution>
						<id>generate-informers</id>
						<phase>generate-sources</phase>
						<goals>
							<goal>generate-informers</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>build-helper-maven-plugin</artifactId>
				<version>1.1</version>
				<executions>
					<execution>
						<id>add-source</id>
						<phase>generate-sources</phase>
						<goals>
							<goal>add-source</goal>
						</goals>
						<configuration>
							<sources>
								<source>target/generated/informers</source>
							</sources>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>

Long, isn't it ? but don't be confused by its length, it's in fact quite simple. Two plugins are used here. The first one is our `gaedo-informer-generator` which, according to its configuration, generates informers in its default location (`target/generated/informers`) which `build-helper-maven-plugin` then adds as source path.

But, there is more. If you look at `gaedo-informer-generator` configuration,you will see some interesting points. indeed, in this execution, `gaedo-informer-generator` adds informers for all classes implementing the Serializable interface. Notice it is also possible to inform classes having a given annotation (by using ... `requiredAnnotation` mojo parameter). Take a look at [mojo full documentation][1], and you'll discover some interesting configuration options.

# What informers look like ? #

Well, generated informers are a little more sophisticated than your hand-written ones (we will take the `Post`/`PostInformer` example). In fact, they are splitted in two parts

 1. a main interface (in our case `PostInformer`) that is to be used by services
 2. an abstract interface (in our case `InternalGaedoPostInformer`), that allows subclasses to also use that class field informers.

Concering that super interface, let me ellaborate a little. it is required for our `PostInformer` to extend `Informer<Post>`. But to allow easy search on elements declared by `Identified`, `PostInformer` should also extend `IdentifiedInformer` which unfortunatly extends `Informer<Identified>`. Thanks to Java generics, extending the same interface with different generics parameters is not possible. As a consequence `PostInformer` can't extend both interfaces. However, `PostInformer` and `IdentifiedInformer` can both extend an `InternalGaedo` (I found no better name, sorry) interface declaring access to class `FieldInformers`. This way, those `FieldInformers` can also be used in subclasses, which is precisely the point of ["Generated informers don't include parent classes informers methods"][2].

Which is why there is this weird idiom used all over gaedo informer generation.


  [1]: plugin-info.html
  [2]: https://github.com/Riduidel/gaedo/issues/14
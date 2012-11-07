Well, the title may be a little overkill (because it only works - for now - using `gaedo-google-datastore`), however I'm quite proud of that feature ! 

Anyway, here come the thingy. As christophe suggests in issue 34, there are some cases when applications have long-life data. Particuarly on google app engine, where an application can be endlessly redeployed with updated code. In such a case, mapping of object fields to datastore content may not work. Well, this time could be over. 

I've indeed decided to add support for object migration to gaedo. The goal is to mimic on live data behaviour possible through frameworks like Rails migration. 

# The concept 

Conceptually, this is quite simple : each time an object is stored, a special field containing its version is stored alongside. When this object is reloaded from datastore (or any other storing scheme that supports this behaviour), we check that this data version has the same value that the object class. If it is not the case, a migration method is called. 

Simple isn't it ? 

Well, the reality is a little more scaring :-) 
# The implementation 

To make that work, your class must say it is "migrable". I've chosen this word to make it sure nobody confuse it with versionning mechanism, or auditing as it is also called. 

For that, you simply annotate your class with the @Migrable annotation : 

    @Migrable(migratorClass=MigrableMigrator.class)
    public class MigrableObject {
            public static final long serialVersionUID = 1;
    
            @Id
            public long id;
    
            public int version = serialVersionUID;
    
            public String text = "initial";
    }

Notice I use a non static field, as the implementation I use to test that feature is `gaedo-google-datastore`, which won't persist static fields. 

Anyway, most of the interesting code is in Migrator class, in the case the MigrableMigrator : 

    public class MigrableMigrator implements Migrator {
    
            @Override
            public Object getCurrentVersion() {
                    return MigrableObject.serialVersionUID;
            }
    
            @Override
            public String getLiveVersionFieldName() {
                    return "version";
            }
    
            @Override
            public String getPersistedVersionFieldName() {
                    return Migrator.DEFAULT_PERSISTED_VERSION_FIELD_NAME;
            }
    
            /**
             * Not usual implementation : when given a MigrableObject, it updates the version to an higher one and totally replace text
             */
            @Override
            public <DataContent, ContainedClass> DataContent migrate(
                            FinderCrudService<ContainedClass, ? extends Informer<ContainedClass>> service,
                            DataContent nonMigratedContent, Object sourceVersion,
                            Object targetVersion) {
                    if(nonMigratedContent instanceof Entity) {
                            Entity toMigrate = (Entity) nonMigratedContent;
                            toMigrate.setProperty( Utils.getDatastoreFieldName( service.getInformer().get( getLiveVersionFieldName() ).getField() ), MigrableObject.serialVersionUID);
                            return (DataContent) toMigrate;
                    }
                    return null;
            }
    }

I won't dive into one-lines methods, as they are direct implementations of Migrator interface, and as a consequence totally defined by this interface. instead, I will go deeper in migrate method. 

As one may note, this method, although present in a class linked to MigrableObject (my model) only works on data serialized from a particular implementation of FinderCrudService. 

This although not optimal, has been considered for now as efficient enough. Later, I may change that behaviour. Anyway, in migrate code, you'll only be able to change the content of data objects, this is why we work here on GAE Entity objects. There are, as a consequence, some friction between this method code and Datastore finder service. As a example, data associated to given fields are accesse

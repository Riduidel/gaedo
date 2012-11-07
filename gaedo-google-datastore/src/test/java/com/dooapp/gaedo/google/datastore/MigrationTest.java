package com.dooapp.gaedo.google.datastore;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.repository.ServiceRepository;

public class MigrationTest {
	private ServiceRepository repository;

	@Before
	public void setUp() {
		AbstractDataStoreTest.setUp();
		repository = TestEnvironmentProvider.create();
	}

	@After
	public void tearDown() {
		AbstractDataStoreTest.tearDown();
	}

	/**
	 * This test uses the totally wrong implemention of migration by
	 * {@link MigrableObject} to do some tests on calls done
	 */
	@Test
	public void testMigration() {
		FinderCrudService<MigrableObject, MigrableObjectInformer> service = repository
				.get(MigrableObject.class);
		MigrableObject toSave = new MigrableObject();
		final MigrableObject saved = service.create(toSave);
		// We update the constant used by the migrator to maintain current version, in order for MigrableMigrator version migrate method to be called
		MigrableObject.storageVersion++;
		// Then, we reload object from datastore
		MigrableObject loaded = service.find().matching(new QueryBuilder<MigrableObjectInformer>() {
			
			@Override
			public QueryExpression createMatchingExpression(
					MigrableObjectInformer informer) {
				return informer.getId().equalsTo(saved.id);
			}
		}).getFirst();
		// Version and text should have been updated
		Assert.assertThat(loaded.version, Is.is(MigrableObject.storageVersion+1));
		Assert.assertThat(loaded.text, Is.is(MigrableMigrator.TEXT_UPDATED_BY_MIGRATOR));
	}
}

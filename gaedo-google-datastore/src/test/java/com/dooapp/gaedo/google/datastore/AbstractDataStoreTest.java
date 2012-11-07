package com.dooapp.gaedo.google.datastore;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Ignore;

import com.google.appengine.api.datastore.dev.LocalDatastoreService;
import com.google.appengine.tools.development.ApiProxyLocalImpl;
import com.google.apphosting.api.ApiProxy;

@Ignore
public class AbstractDataStoreTest {

	/**
	 * Prepare connection to local datastore, see
	 * http://code.google.com/intl/fr-
	 * FR/appengine/docs/java/howto/unittesting.html for more infos
	 */
	@BeforeClass
	public static void setUp() {
		ApiProxy.setEnvironmentForCurrentThread(new TestEnvironment());
		ApiProxy.setDelegate(new ApiProxyLocalImpl(new File(".")) {
		});
		ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
		proxy.setProperty(LocalDatastoreService.NO_STORAGE_PROPERTY,
				Boolean.TRUE.toString());
	}

	// @AfterClass
	public static void tearDown() {
		ApiProxyLocalImpl proxy = (ApiProxyLocalImpl) ApiProxy.getDelegate();
		LocalDatastoreService datastoreService = (LocalDatastoreService) proxy
				.getService(LocalDatastoreService.PACKAGE);
		datastoreService.clearProfiles();
		ApiProxy.setDelegate(null);
		ApiProxy.setEnvironmentForCurrentThread(null);
	}

}

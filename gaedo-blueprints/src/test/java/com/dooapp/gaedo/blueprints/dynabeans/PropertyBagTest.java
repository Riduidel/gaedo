package com.dooapp.gaedo.blueprints.dynabeans;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphTest;
import com.dooapp.gaedo.blueprints.TestUtils;
import com.dooapp.gaedo.blueprints.queries.tests.CollectionContains;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.finders.informers.CollectionFieldInformer;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.utils.CollectionUtils;

import static org.junit.Assert.*;

/**
 * Read the lite.nasa.nt file into a graph that will be read by a gaedo-blueprints finder servcie tied to a DynamicPropertyProvider,
 * giving us the ability to read any graph node, navigating them and so on ...
 * @author ndx
 *
 */
@RunWith(Parameterized.class)
public class PropertyBagTest extends AbstractGraphTest {

	private final class FindByDiscipline implements QueryBuilder<PropertyBagInformer> {
		private final PropertyBagMap discipline;

		private FindByDiscipline(PropertyBagMap discipline) {
			this.discipline = discipline;
		}

		@Override
		public QueryExpression createMatchingExpression(PropertyBagInformer informer) {
			return ((CollectionFieldInformer) informer.get(DYNAMIC_DISCIPLINE)).containing(discipline);
		}
	}

	private static final String ENGINEERING = "http://nasa.dataincubator.org/discipline/engineering";
	private static final String ARIANE_L01_WIKIEDIA_DESCRIPTION = "Ariane 1 was the first rocket in the Ariane launcher family. Ariane 1 was designed primarily to put two telecommunications satellites at a time into orbit, thus reducing costs. As the size of satellites grew, Ariane 1 gave way to the more powerful Ariane 2 and Ariane 3 launchers.";
	private static final String ARIANE_L01 = "http://nasa.dataincubator.org/spacecraft/ARIANEL01";
	private static final String SATURN_SA_1_WIKIPEDIA_DESCRIPTION = "SA-1 was the first Saturn I space launch vehicle, the first in the Saturn family, and first mission of the American Apollo program. The rocket was launched on October 27, 1961 from Cape Canaveral, Florida.";
	private static final String DYNAMIC_DESCRIPTION = "http://purl.org/dc/elements/1.1/description";
	private static final String RUSSIAN_033B = "http://nasa.dataincubator.org/spacecraft/1989-033B";
	private static final String PION2 = "http://nasa.dataincubator.org/spacecraft/PION2";
	private static final String SATURN_SA1 = "http://nasa.dataincubator.org/spacecraft/SATURNSA1";
	private static final String PLANETARY_SCIENCE = "http://nasa.dataincubator.org/discipline/planetaryscience";
	private static final String DYNAMIC_DISCIPLINE = "http://purl.org/net/schemas/space/discipline";
	private static final String DYNAMIC_NAME = "http://xmlns.com/foaf/0.1/name";
	/**
	 * File from which data will be loaded
	 */
	private static File source;

	@Parameters
	public static Collection<Object[]> parameters() {
		return TestUtils.simpleTest();
	}

	@BeforeClass
	public static void loadSource() throws Exception {
		source = new File(PropertyBagTest.class.getClassLoader().getResource("lite.nasa.nt").toURI());
		
	}

	private FinderCrudService<PropertyBagMap, PropertyBagInformer> propertyBagService;

	public PropertyBagTest(AbstractGraphEnvironment<?> graph) {
		super(graph);
	}
	
	@Before
	public void prepareAll() throws Exception {
		loadData();
		customizeEnvironment();
		// now data has been added, create a dynamic service
		propertyBagService = 
						environment.createServiceFor(PropertyBagMap.class, PropertyBagInformer.class, StrategyType.graphBased);
	}
	
	/**
	 * Customize gaedo service repository to allow working
	 */
	private void customizeEnvironment() {
	}

	public void loadData() throws RepositoryException, RDFParseException, IOException {
		SailRepository repository = getRepository();
		repository.initialize();
		repository.getConnection().add(source, null, null);
	}

	@Test
	public void testFind_SaturnSA1() {
		if (propertyBagService instanceof IdBasedService) {
			IdBasedService<PropertyBagMap> idService = (IdBasedService<PropertyBagMap>) propertyBagService;
			PropertyBag map = idService.findById(SATURN_SA1);
			assertThat(map, IsNull.notNullValue());
			assertThat(map.getId(), Is.is(SATURN_SA1));
			assertThat(map.get(DYNAMIC_NAME).toString(), Is.is("Saturn SA-1"));
			assertThat(map.get(DYNAMIC_DISCIPLINE), Is.is(PropertyBag.class));
			assertThat(((PropertyBag) map.get(DYNAMIC_DISCIPLINE)).getId(), Is.is(PLANETARY_SCIENCE));
		} else {
			fail("service should be id based \"by design\"");
		}
	}
	
	@Test
	public void test_Find_All_Planteray_Science_spacecrafts() {
		final PropertyBagMap planetaryScience = ((IdBasedService<PropertyBagMap>) propertyBagService).findById(PLANETARY_SCIENCE);
		Iterable<PropertyBagMap> values = propertyBagService.find().matching(new FindByDiscipline(planetaryScience)).getAll();
		
		List<PropertyBagMap> valuesList = CollectionUtils.asList(values);
		assertThat(valuesList, IsNull.notNullValue());
		assertThat(valuesList.size(), Is.is(3));
		
		Collection<String> planetaryScienceSpacecrafts = Arrays.asList(SATURN_SA1, PION2, RUSSIAN_033B);
		for(PropertyBagMap value: valuesList) {
			assertThat(planetaryScienceSpacecrafts, IsCollectionContaining.hasItem(value.getId()));
		}
	}
	
	@Test
	public void test_Update_Saturn_SA_1_Description() {
		if (propertyBagService instanceof IdBasedService) {
			IdBasedService<PropertyBagMap> idService = (IdBasedService<PropertyBagMap>) propertyBagService;
			PropertyBagMap map = idService.findById(SATURN_SA1);
			assertThat(map, IsNull.notNullValue());
			assertThat(map.getId(), Is.is(SATURN_SA1));
			map.set(DYNAMIC_DESCRIPTION, SATURN_SA_1_WIKIPEDIA_DESCRIPTION);
			idService.update(map);
			// reload it
			map = idService.findById(SATURN_SA1);
			assertThat(map, IsNull.notNullValue());
			assertThat(map.getId(), Is.is(SATURN_SA1));
			assertThat(map.get(DYNAMIC_DESCRIPTION), Is.is((Object) SATURN_SA_1_WIKIPEDIA_DESCRIPTION));
		} else {
			fail("service should be id based \"by design\"");
		}
	}
	
	@Test
	public void test_create_Ariane1_first_fly_works() {
		if (propertyBagService instanceof IdBasedService) {
			final IdBasedService<PropertyBagMap> idService = (IdBasedService<PropertyBagMap>) propertyBagService;
			PropertyBagMap ariane1 = idService.create(ariane1(idService.findById(ENGINEERING)));
			// reload it
			ariane1 = idService.findById(ARIANE_L01);
			assertThat(ariane1, IsNull.notNullValue());
			assertThat(ariane1.getId(), Is.is(ARIANE_L01));
			assertThat(ariane1.get(DYNAMIC_DESCRIPTION), Is.is((Object) ARIANE_L01_WIKIEDIA_DESCRIPTION));
			// make sure it is a spaceship studying engineering (I love that test sample dataset)
			Iterable<PropertyBagMap> engineeringSpaceships = propertyBagService.find().matching(new FindByDiscipline(idService.findById(ENGINEERING))).getAll();
			List<PropertyBagMap> spaceshipList = CollectionUtils.asList(engineeringSpaceships);
			assertThat(spaceshipList, IsCollectionContaining.hasItem(ariane1));
		} else {
			fail("service should be id based \"by design\"");
		}
	}
	
	@Test
	public void test_create_Ariane1_then_delete_it_works() {
		if (propertyBagService instanceof IdBasedService) {
			final IdBasedService<PropertyBagMap> idService = (IdBasedService<PropertyBagMap>) propertyBagService;
			PropertyBagMap engineering = idService.findById(ENGINEERING);
			PropertyBagMap ariane1 = ariane1(engineering);
			ariane1 = idService.create(ariane1);
			// make sure it is a spaceship studying engineering (I love that test sample dataset)
			Iterable<PropertyBagMap> engineeringSpaceships = propertyBagService.find().matching(new FindByDiscipline(idService.findById(ENGINEERING))).getAll();
			List<PropertyBagMap> spaceshipList = CollectionUtils.asList(engineeringSpaceships);
			assertThat(spaceshipList, IsCollectionContaining.hasItem(ariane1));
			// ariane1 is not "valid"
			int numberOfvalidSpacesihips = spaceshipList.size()-1;
			// now delete it
			idService.delete(ariane1);
			engineeringSpaceships = propertyBagService.find().matching(new FindByDiscipline(idService.findById(ENGINEERING))).getAll();
			spaceshipList = CollectionUtils.asList(engineeringSpaceships);
			assertThat(spaceshipList.size(), Is.is(numberOfvalidSpacesihips));
		} else {
			fail("service should be id based \"by design\"");
		}
	}

	private PropertyBagMap ariane1(PropertyBagMap engineering) {
		PropertyBagMap map = new PropertyBagMap().withId(ARIANE_L01);
		map.set(DYNAMIC_DESCRIPTION, ARIANE_L01_WIKIEDIA_DESCRIPTION);
		map.set(DYNAMIC_DISCIPLINE, engineering);
		map.set(DYNAMIC_NAME, "Ariane 1 - L-01");
		return map;
	}

	@Test
	public void testLoadAll() {
		Iterable<PropertyBagMap> all = propertyBagService.findAll();
		List<PropertyBagMap> allList = CollectionUtils.asList(all);
		assertThat(allList, IsNull.notNullValue());
		assertThat(allList.size(), IsNot.not(Is.is(0)));
	}
	
}

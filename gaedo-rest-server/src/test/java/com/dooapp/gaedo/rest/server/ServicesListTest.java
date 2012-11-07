package com.dooapp.gaedo.rest.server;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.custommonkey.xmlunit.AbstractNodeTester;
import org.custommonkey.xmlunit.NodeTest;
import org.custommonkey.xmlunit.NodeTestException;
import org.custommonkey.xmlunit.NodeTester;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.User;


public class ServicesListTest extends AbstractGaedoResourceApplicationTest {
	@Test
	public void testServicesListInXml() throws Exception {
		final Collection<Class<? extends Serializable>> containedClasses = new LinkedList<Class<? extends Serializable>>(Arrays.asList(User.class, Post.class));
		ClientResource serviceList = new ClientResource("http://localhost:"+TEST_PORT+TEST_DIR+GaedoResourceApplication.SERVICE_LIST_ROUTE);
		Representation representation = serviceList.get(MediaType.TEXT_XML);
		Assert.assertThat(representation.getMediaType(), Is.is(MediaType.TEXT_XML));
		NodeTest nodeTest = new NodeTest(representation.getReader());
		NodeTester nodeTester = new AbstractNodeTester() {
			@Override
			public void testElement(Element element) throws NodeTestException {
				String nodeName = element.getNodeName();
				if(ServicesList.SERVICE_LIST_NODE_NAME.equals(nodeName)) {
					if(element.getAttributes().getLength()>0) {
						throw new NodeTestException("element "+ServicesList.SERVICE_LIST_NODE_NAME+" is not expected to have any attribute");
					}
				} else if(ServicesList.SERVICE_NODE_NAME.equals(nodeName)) {
					String dataType = element.getAttribute(ServicesList.DATA_TYPE_ATTRIBUTE);
					/*
					 * Unchecked code is used here to simplify some rather complex calls
					 */
					try {
						Class containedType = Class.forName(dataType);
						containedClasses.remove(containedType);
						FinderCrudService service = repository.get(containedType);
						Class serviceInterface = Class.forName(element.getAttribute(ServicesList.SERVICE_TYPE_ATTRIBUTE));
						if(!serviceInterface.isInstance(service)) {
							throw new NodeTestException("service "+service+" is not an instance of interface "+serviceInterface.getName()+" which designates it", element);
						}
						Class informerType = Class.forName(element.getAttribute(ServicesList.INFORMER_TYPE_ATTRIBUTE));
						if(!informerType.isInstance(service.getInformer())) {
							throw new NodeTestException("service informer "+service.getInformer()+" is not an instance of interface "+informerType.getName()+" which designates it", element);
						}
					} catch(NodeTestException e) {
						throw e;
					} catch(Exception e) {
						e.printStackTrace();
						throw new NodeTestException("something went wrong while testing node due to inner exception "+e.getMessage(), element);
					}
				} else {
					throw new NodeTestException("no node name other than "+ServicesList.SERVICE_LIST_NODE_NAME+" and "
							+ServicesList.SERVICE_NODE_NAME+" is allowed in this document", element);
				}
			}
		};
		nodeTest.performTest(nodeTester, Node.ELEMENT_NODE);
		Assert.assertThat(containedClasses.size(), Is.is(0));
	}
	
	@Test
	public void testServciesListInJson() throws Exception {
		ClientResource serviceList = new ClientResource("http://localhost:"+TEST_PORT+TEST_DIR+GaedoResourceApplication.SERVICE_LIST_ROUTE);
		Representation representation = serviceList.get(MediaType.APPLICATION_JSON);
		Assert.assertThat(representation.getMediaType(), Is.is(MediaType.APPLICATION_JSON));
	}
}

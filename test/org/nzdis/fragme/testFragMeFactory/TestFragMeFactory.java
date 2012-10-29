package org.nzdis.fragme.testFragMeFactory;

import java.util.Hashtable;
import java.util.Vector;
import junit.framework.TestCase;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.ObjectManager;
import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;

/**
 * Tests features of the FMeObject class.
 * 
 * @author Frank Wu
 * 
 */
public class TestFragMeFactory extends TestCase {

	/**
	 * FMeObject class variable
	 */
	public FMeObject o = null;

	/**
	 * Sets up a new connection for each test case.
	 */
	public void setUp() {
		new TestClass2();
		ControlCenter.setUpConnections("testGroup1", "testPeer");
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		o = null;
		FragMeFactory.resetFreeObjects();
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests that objects are created with the correct address set, and that its
	 * Id is properly set.
	 */
	public void testCreateNewObject() {
		try {
			o = FragMeFactory.createNewObject(TestClass2.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(o);
		ObjectManager OM = ControlCenter.getObjectManager();
		assertSame("Owner address should be the same as my address", o
				.getOwnerAddr(), OM.getMyAddress());
	}

	/**
	 * Test that objects are stored in freeObjects list with the correct type
	 * when they are freed. And that the list size does not exceed
	 * maxNumberFreeObjects.
	 */
	public void testFreeObject() {
		// Test Objects stored when they are freed
		try {
			o = (FMeObject) FragMeFactory.createNewObject(TestClass2.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Hashtable freeObjects = FragMeFactory.getFreeObjects();
		Vector list = (Vector) freeObjects.get(TestClass2.class);
		assertNull("The list of freeObjects should be null", list);
		FragMeFactory.freeObject(o, TestClass2.class);
		freeObjects = FragMeFactory.getFreeObjects();
		list = (Vector) freeObjects.get(TestClass2.class);
		assertNotNull("The list of freeObjects should not be null", list);

		// Keep freeing objects until the number of objects reaches the max
		// number of free objects.
		int expectedSize = 1;
		assertEquals("The number of free objects is incorrect", expectedSize,
				list.size());
		assertSame("Wrong object returned", o, list.get(0));
		try {
			int maxFreeObjects = FragMeFactory.getMaxNumOfFreeObjects();
			while (list.size() < maxFreeObjects) {
				FragMeFactory.freeObject(o, TestClass2.class);
				list = (Vector) freeObjects.get(TestClass2.class);
				// assertEquals(2, freeObjectsSize);
				expectedSize++;
				assertEquals("The number of free objects is incorrect",
						expectedSize, list.size());
				assertSame("Wrong object returned", o, list
						.get(list.size() - 1));
			}
			FragMeFactory.freeObject(o, TestClass2.class);
			list = (Vector) freeObjects.get(TestClass2.class);
			assertEquals(
					"The size of the list should be the same as maxFreeObjects",
					maxFreeObjects, list.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test that deserialize is working properly by testing that the object is
	 * retrieved when getOwnObjects() is called. Test that if an existing object
	 * is deserialized, the existing object is returned and is added to
	 * freeObjects vector.
	 */
	public void testDeserialize() {
		try {
			//
			o = new TestClass2();
			ObjectManager OM = ControlCenter.getObjectManager();
			o.setOwnerAddr(OM.getMyAddress());
			o.setId(FragMeFactory.generateObjectId());
			o = FragMeFactory.deserialize(o);
			Vector objs = OM.getOwnObjects();
			assertTrue("Own objects not is in vector", objs.contains(o));

			o = FragMeFactory.createNewObject(TestClass2.class);
			o = FragMeFactory.deserialize(o);
			Hashtable freeObjects = FragMeFactory.getFreeObjects();
			Vector list = (Vector) freeObjects.get(TestClass2.class);
			assertEquals("The number of free objects is incorrect", 1,
					freeObjects.size());
			assertEquals(
					"The number of free objects that are instances of TestClass2 is incorrect",
					1, list.size());
			assertEquals(
					"Wrong object returned in the freeObjects returned from FragMeFactory.getFreeObjects()",
					o, list.get(0));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * generateObjectId() increments Id by 1 - a simple method, does not require
	 * testing.
	 */
	public void testGenerateObjectId() {
	}

	/**
	 * addFactory() puts factory into hashmap - a simple method, does not reuire
	 * testing.
	 */
	public void testAddFactory() {
	}

}

/**
 * Dummy class used to create a child instance of FMeObject for testing.
 * 
 * @author Frank Wu
 * 
 */
class TestClass2 extends FMeObject {
	private transient static final long serialVersionUID = -5494387548142630381L;

	public void deserialize(final FMeObject serObject) {
	}

	static {
		FragMeFactory.addFactory(new Factory(), TestClass2.class);
	}

	private static class Factory extends FragMeFactory {
		protected FactoryObject create() {
			return new TestClass2();
		}
	}

	@Override
	public void changed(FMeObject object) {
	}

	@Override
	public void delegatedOwnership(FMeObject object) {
	}

	@Override
	public void deleted(FMeObject object) {
	}
}

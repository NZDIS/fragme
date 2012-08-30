package org.nzdis.fragme.testControlCenter;

import java.io.IOException;
import java.util.Vector;
import junit.framework.TestCase;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.util.DetermineOS;

/**
 * A test suite containing tests of object management/retrieval
 * functionality.  Each test must be run non-sequentially, as 
 * synchronisation must be achieved with the second JVM used by
 * most of the tests within this class.  This JVM runs 
 * TestGetObjectPeer.jar, a simple second peer that manages some
 * objects and runs for a set time period.
 * 
 * @author Morgan Bruce
 *
 */
public class TestGetObjects extends TestCase {
	/**
	 * Path of TestGetObjectPeer that creates new peers in another java virtual
	 * machine. Path depends on where TestGetObjectPeer.jar is located on the
	 * file system
	 */
	
	static String path = "java -jar ";

	//building path to external jar
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestGetObjectPeer.java as main class
		 */
		String subPath = TestGetObjects.class.getResource("TestGetObjectsPeer.jar").getPath();
		if(DetermineOS.getOS().equals(DetermineOS.WINDOWS)){
			subPath = subPath.substring(1);
			path = "cmd /C start " + path;
		}
		path += subPath;
		System.out.println("Path to multiple peer test jar file: " + path);
		
		//explicitly instantiating of TestObject classes to ensure registration with FragMeFactory
		new TestObjectA();
		new TestObjectB();
		new TestObjectC();
	}
	
	/**
	 * Sets up a new connection for each test case.
	 */
	public void setUp() {
		ControlCenter.setUpConnections("testGroup1", "testPeer");
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		try {
			int i = 30;
			System.out.println("Wait " + i + " secs");
			while (i > 0) {
				Thread.sleep(1000);
				if (i % 5 == 0)
					System.out.println();
				System.out.print(" " + i);
				i--;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests that the retrieve all objects functionality works correctly
	 * (ControlCenter.getAllObjects())
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetAllObjects() throws IOException, InterruptedException {
		TestObjectA a = (TestObjectA) ControlCenter
				.createNewObject(TestObjectA.class);
		a.setDummy("hello1");
		TestObjectB b = (TestObjectB) ControlCenter
				.createNewObject(TestObjectB.class);
		b.setDummy("hello2");

		Vector v = ControlCenter.getAllObjects();
		System.out.println(v.size());
		for (int i = 0; i < v.size(); i++) {
			System.out.println(v.get(i));
		}
		assertEquals("The number of all objects returned is incorrect", 2, v
				.size());

		Runtime.getRuntime().exec(path);
		System.out.println("Wait 10 secs");
		Thread.sleep(10000);
		v = ControlCenter.getAllObjects();
		for (int i = 0; i < v.size(); i++) {
			System.out.println(v.get(i));
		}
		assertEquals(
				"the number of all objects returned (after creating another object by another peer) is incorrect",
				3, v.size());
		System.out.println(v.size());
	}

	/**
	 * Tests that getting all objects of a particular class works correctly.
	 * (ControlCenter.getAllObjects(Class))
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetAllObjectsOfClass() throws IOException,
			InterruptedException {
		TestObjectA a = (TestObjectA) ControlCenter
				.createNewObject(TestObjectA.class);
		a.setDummy("hello1");
		TestObjectB b = (TestObjectB) ControlCenter
				.createNewObject(TestObjectB.class);
		b.setDummy("hello2");
		TestObjectC c = (TestObjectC) ControlCenter
				.createNewObject(TestObjectC.class);
		c.setDummy("hello3");

		Process runningFragMeInstance = Runtime.getRuntime().exec(path);
		System.out.println("Wait 10 secs");
		Thread.sleep(10000);
		Vector v = ControlCenter.getAllObjects(TestObjectC.class);
		assertEquals("The number of objects of TestObjectC class is incorrect",
				2, v.size());
	}

	/**
	 * Tests that getting a peer's own objects of a particular class
	 * works correctly
	 * (ControlCenter.getOwnObjects(Class))
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetOwnObjectsOfClass() throws IOException,
			InterruptedException {
		TestObjectA a = (TestObjectA) ControlCenter
				.createNewObject(TestObjectA.class);
		a.setDummy("hello1");
		TestObjectB b = (TestObjectB) ControlCenter
				.createNewObject(TestObjectB.class);
		b.setDummy("hello");
		TestObjectC c = (TestObjectC) ControlCenter
				.createNewObject(TestObjectC.class);
		c.setDummy("hello3");

		Runtime.getRuntime().exec(path);
		System.out.println("Wait 10 secs");
		Thread.sleep(10000);
		Vector v = ControlCenter.getAllObjects(TestObjectC.class);
		assertEquals(
				"The number of all objects of TestObjectC class is incorrect",
				2, v.size());

		v = ControlCenter.getOwnObjects(TestObjectC.class);
		assertEquals(
				"The number of own objects of TestObjectC class is incorrect",
				1, v.size());
	}

	/**
	 * Tests that retrieving all of a peer's own objects works correctly
	 * (ControlCenter.getOwnObjects())
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testGetOwnObjects() throws IOException, InterruptedException {
		TestObjectA a = (TestObjectA) ControlCenter
				.createNewObject(TestObjectA.class);
		a.setDummy("hello1");

		// multi peer
		Runtime.getRuntime().exec(path);
		System.out.println("Wait 10 secs");
		Thread.sleep(10000);
		Vector v = ControlCenter.getAllObjects();
		assertEquals("The number of all objects is incorrect", 2, v.size());

		v = ControlCenter.getOwnObjects();
		assertEquals(
				"The own object returned is not the same as the object created",
				a, v.get(0));
		assertEquals("The owner address of the object is incorrect", a
				.getOwnerAddr(), ((TestObjectA) v.get(0)).getOwnerAddr());
		assertEquals("The number of own objects is incorrect", 1, v.size());
	}
}

package org.nzdis.fragme.testControlCenter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import junit.framework.TestCase;
import org.nzdis.fragme.ControlCenter;
import org.jgroups.Address;

/**
 * Runs JUnit tests on public methods of the FragMe ControlCenter
 * 
 * @author Morgan Bruce, Frank Wu
 * 
 */
public class TestControlCenter extends TestCase {

	static String path = "java -jar ";
	
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestClass.java as main class
		 */
		path += TestGetObjects.class.getResource("TestClass.jar").getPath().substring(1);
		System.out.println("Path to multiple peer test jar file: " + path);
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
		ControlCenter.closeUpConnections();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Waiting 5 seconds for channel to close down.");
	}

	/**
	 * Tests ControlCenter.setUpConnections(), ensuring that connections are
	 * correctly set up.
	 */
	public void testSetupConnections() {
		ControlCenter.closeUpConnections();
		ControlCenter.setUpConnections("testGroup1", "testPeer1");
		assertNotNull("getObjectManager() should not return null",
				ControlCenter.getObjectManager());
		assertNotNull("getPeerManager() should not return null", ControlCenter
				.getPeerManager());
	}

	/**
	 * Tests ControlCenter.getMyAddress(), to ensure that a peer can retrieve
	 * their own address, and that this is the correct address
	 */
	public void testGetMyAddress() {
		Address x = ControlCenter.getMyAddress();

		// ensure address belongs to this peer - not the greatest test, but
		// seems to be only way..

		assertEquals(
				"Parsing in own address in getMyPeerName should return own peer name",
				"testPeer", ControlCenter.getPeerName(x));

	}

	/**
	 * Tests ControlCenter.getPeerName() to ensure the peer can retrieve the
	 * correct peer name for another peer
	 * 
	 */
	public void testGetPeerName() {
		assertEquals(
				"Parsing in own address in getMyPeerName should return own peer name", "testPeer",
				ControlCenter.getPeerName(ControlCenter.getMyAddress()));
	}

	/**
	 * Tests ControlCenter.getGroupName() ensuring that the group name can be
	 * retrieved correctly
	 */
	public void testGetGroupName() {
		assertEquals("Incorrect group name returned", "testGroup1",
				ControlCenter.getGroupName());
	}

	/**
	 * Tests closeUpConnections()
	 */
	public void testCloseUpConnections() {
		ControlCenter.closeUpConnections();
		assertNull("getPeerManager should return null after connection closed",
				ControlCenter.getPeerManager());
		assertNull(
				"getObjectManager should return null after connection closed",
				ControlCenter.getObjectManager());

		/** set up again so tearDown doesn't fail */
		ControlCenter.setUpConnections("testGroup1", "testPeer");
	}

	/**
	 * Tests serialize to file by saving a String
	 */
	public void testSerializeString() {
		String toSave = "hello";
		Object toSave2 = "Hi";

		ControlCenter.removeAllFromList();
		ControlCenter.addToList(toSave);
		ControlCenter.addToList(toSave2);

		try {
			ControlCenter.serializedToFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String loaded = "";
		String loaded2 = "";

		try {
			FileInputStream f = new FileInputStream("objectsOnDisk.txt");
			Vector v1 = ControlCenter.serializedFromFile(String.class);
			loaded = (String) v1.get(0);
			loaded2 = (String) v1.get(1);
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(
				"String to be saved should be saved and loaded properly and should equal",
				toSave, loaded);
		assertEquals(
				"String to be saved should be saved and loaded properly and should equal",
				toSave2, loaded2);
	}

	/**
	 * Tests serialize to file by saving an Object
	 */
	public void testSerializeObject() {
		TestClass toSave = new TestClass();
		TestClass loaded = null;

		ControlCenter.removeAllFromList();
		ControlCenter.addToList(toSave);

		try {
			ControlCenter.serializedToFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileInputStream f = new FileInputStream("objectsOnDisk.txt");
			Vector v1 = ControlCenter.serializedFromFile(TestClass.class);
			loaded = (TestClass) v1.get(0);
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(
				"Object to be saved should be saved and loaded properly and should equal",
				toSave.test, loaded.test);
		assertEquals(
				"Object to be saved should be saved and loaded properly and should equal",
				toSave.testInt, loaded.testInt);
	}

	/**
	 * Tests ControlCenter.getNoOfPeers(), ensuring that the number of peers
	 * returned is equal to the number of peers within the network
	 */
	public void testGetNoOfPeers() throws IOException, InterruptedException {
		assertEquals(0, ControlCenter.getNoOfPeers());

		// String file = "java -jar
		// c:\\eclipse\\workspace\\fragme\\src\\tests\\testControlCenter\\test2.
		// jar";
		//String file = "java -jar s:\\Eclipse33\\fragme\\src\\tests\\testControlCenter\\test2.jar";
		Runtime.getRuntime().exec(path);
		System.out.println("Wait 10 secs");
		Thread.sleep(10000);
		assertEquals("Peer count incorrect", 1, ControlCenter.getNoOfPeers());
		Runtime.getRuntime().exec(path);
		System.out.println("Wait 10 secs");
		Thread.sleep(10000);
		assertEquals(
				"New peer not joined or getNoOfPeers() not returning the right number of peers",
				2, ControlCenter.getNoOfPeers());
		System.out.println("Wait 30 secs");
		Thread.sleep(30000);
	}

	/**
	 * Tests ControlCenter.setUpConnections(String groupName), ensuring that a
	 * connection can be set up without explicity defining a peer name at
	 * invocation
	 */
	public void testSetUpConnectionsNoPeerName() {
		ControlCenter.closeUpConnections();
		ControlCenter.setUpConnections("testGroup");
		assertNotNull("getObjectManager() should not return null",
				ControlCenter.getObjectManager());
		assertNotNull("getPeerManager() should not return null", ControlCenter
				.getPeerManager());
	}
}

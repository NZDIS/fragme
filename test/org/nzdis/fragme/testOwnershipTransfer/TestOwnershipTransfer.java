package org.nzdis.fragme.testOwnershipTransfer;

import java.io.IOException;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.util.DetermineOS;

import junit.framework.TestCase;

/**
 * This class tests the ownership delegation functionalities implemented in FMeObjects.
 * 
 * @author Nathan Lewis
 */
public class TestOwnershipTransfer extends TestCase {

	public static final String TestGroupName = "jUnitTestOwnershipTransfer";
	public static final String TestOwnershipOtherClient = "TestOwnershipOtherClient";
	public static final String TestOwnershipTester = "TestOwnershipTester";
	
	static final Integer startupDelay = 5000;
	static final Integer waitForOtherClientDelay = 10000;
	static String path = "java -jar ";
	
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestClass.java as main class
		 */
		String subPath = TestOwnershipTransfer.class.getResource(TestOwnershipOtherClient.class.getSimpleName() + ".jar").getPath();
		if(DetermineOS.getOS().equals(DetermineOS.WINDOWS)) {
			subPath = subPath.substring(1);
			path = "cmd /C start " + path;
		} else if(DetermineOS.getOS().equals(DetermineOS.LINUX)) {
			path += "-Djava.net.preferIPv4Stack=true ";
		}
		path += subPath;
		//System.out.println("Path to multiple peer test jar file: " + path);
	}
	
	/**
	 * Sets up a new connection for each test case. 
	 */
	public void setUp() {
		ControlCenter.setUpConnections(TestGroupName, TestOwnershipTester);
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests requestOwnership
	 * The tester creates an object. The other client locates that object and sends a
	 * request for ownership of it. The tester accepts that request. The other client should 
	 * now own the object.
	 */
	public void testRequestOwnership() throws InterruptedException, IOException {
		// startup the tester
		new TestOwnershipObject();
		TestOwnershipObject obj = (TestOwnershipObject)ControlCenter.createNewObject(TestOwnershipObject.class);
		assertEquals("The initial owner of the object is incorrect", TestOwnershipTester, obj.getOwnerName());
		obj = null;
		
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the other client (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testRequestOwnership");
		
		// give the other client time to startup and complete it's operations
		System.out.println("waiting for other client " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for other client finished");
		
		// check the results
		obj = (TestOwnershipObject)ControlCenter.getAllObjects(TestOwnershipObject.class).elementAt(0);
		assertEquals("The final owner of the object is incorrect", TestOwnershipOtherClient, obj.getOwnerName());
	}

	/**
	 * Tests delegateOwnership
	 * The other client creates an object and delegates it to the tester. The
	 * tester should now see that it owns the object.
	 */
	public void testDelegateOwnership1() throws InterruptedException, IOException {
		// startup the tester
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the other client (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testDelegateOwnership1");
		
		// give the other client time to startup and complete it's operations
		System.out.println("waiting for other client " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for other client finished");
		
		// check the results
		TestOwnershipObject obj = (TestOwnershipObject)ControlCenter.getAllObjects(TestOwnershipObject.class).elementAt(0);
		assertEquals("The final owner of the object is incorrect", TestOwnershipTester, obj.getOwnerName());
	}

	/**
	 * Tests delegateOwnership
	 * The tester creates an object and delegates it to the other tester. The tester
	 * should now see that the other client owns the object.
	 */
	public void testDelegateOwnership2() throws InterruptedException, IOException {
		// startup the tester
		new TestOwnershipObject();
		TestOwnershipObject obj = (TestOwnershipObject)ControlCenter.createNewObject(TestOwnershipObject.class);
		assertEquals("The initial owner of the object is incorrect", TestOwnershipTester, obj.getOwnerName());
		
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the other client (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testDelegateOwnership2");
		
		// give the other client time to startup
		System.out.println("waiting for other client " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for other client startup");
		
		System.out.println("perform the operation");
		obj.delegateOwnership(TestOwnershipOtherClient);
		
		// give the other client time to complete it's operations
		System.out.println("waiting for other client " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for other client startup");
		
		// check the results
		assertEquals("The final owner of the object is incorrect", TestOwnershipOtherClient, obj.getOwnerName());
	}

}

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
	public static final String TestOwnershipClientSender = "TestOwnershipClientSender";
	public static final String TestOwnershipClientReceiver = "TestOwnershipClientReceiver";
	
	static final Integer startupDelay = 5000;
	static final Integer waitForSenderDelay = 10000;
	static String path = "java -jar ";
	
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestClass.java as main class
		 */
		String subPath = TestOwnershipTransfer.class.getResource(TestOwnershipClientSender.class.getSimpleName() + ".jar").getPath();
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
		ControlCenter.setUpConnections(TestGroupName, TestOwnershipClientReceiver);
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests requestOwnership
	 * The receiver creates an object. The sender locates that object and sends a
	 * request for ownership of it. The receiver accepts that request.
	 */
	public void testRequestOwnership() throws InterruptedException, IOException {
		// startup the receiver
		new TestOwnershipObject();
		TestOwnershipObject obj = (TestOwnershipObject)ControlCenter.createNewObject(TestOwnershipObject.class);
		assertEquals("The initial owner of the object is incorrect", TestOwnershipClientReceiver, obj.getOwnerName());
		obj = null;
		
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testRequestOwnership");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForSenderDelay + "ms");
		Thread.sleep(waitForSenderDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestOwnershipObject)ControlCenter.getAllObjects(TestOwnershipObject.class).elementAt(0);
		assertEquals("The final owner of the object is incorrect", TestOwnershipClientSender, obj.getOwnerName());
	}

	/**
	 * Tests delegateOwnership
	 * The sender creates an object and delegates it to the receiver
	 */
	public void testDelegateOwnership() throws InterruptedException, IOException {
		// startup the receiver
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testDelegateOwnership");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForSenderDelay + "ms");
		Thread.sleep(waitForSenderDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		TestOwnershipObject obj = (TestOwnershipObject)ControlCenter.getAllObjects(TestOwnershipObject.class).elementAt(0);
		assertEquals("The final owner of the object is incorrect", TestOwnershipClientReceiver, obj.getOwnerName());
	}

}

package org.nzdis.fragme.testPermissions;

import java.io.IOException;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.util.DetermineOS;

import junit.framework.TestCase;

/**
 * This class tests the permissions functionalities implemented in FMeObjects.
 * 
 * @author Nathan Lewis
 */
public class TestPermissions extends TestCase {

	public static final String TestGroupName = "jUnitTestPermissions";
	public static final String TestPermissionsClientSender = "TestPermissionsClientSender";
	public static final String TestPermissionsClientReceiver = "TestPermissionsClientReceiver";
	
	static final Integer startupDelay = 5000;
	static final Integer waitForSenderDelay = 10000;
	static String path = "java -jar ";
	
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestClass.java as main class
		 */
		String subPath = TestPermissions.class.getResource(TestPermissionsClientSender + ".jar").getPath();
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
		ControlCenter.setUpConnections(TestGroupName, TestPermissionsClientReceiver);
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests override of allowDeserialize True
	 * The receiver (this) creates an object. The sender finds the object and
	 * modifies it. The sender does not accept the update.
	 */
	public void testAllowDeserializeTrue() throws InterruptedException, IOException {
		// startup the receiver
		new TestPermissionsObject();
		TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
		obj.setValue(1);
		obj = null;

		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowDeserializeTrue");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForSenderDelay + "ms");
		Thread.sleep(waitForSenderDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
		assertEquals("The value of the object at shutdown is incorrect", 2, obj.getValue());
		assertEquals("The request was not received", TestPermissionsClientSender, obj.requesterName);
	}

	/**
	 * Tests override of allowDeserialize False
	 * The receiver (this) creates an object. The sender finds the object and
	 * modifies it. The sender does not accept the update.
	 */
	public void testAllowDeserializeFalse() throws InterruptedException, IOException {
		// startup the receiver
		new TestPermissionsObject();
		TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
		obj.setValue(0);
		obj = null;

		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowDeserializeFalse");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForSenderDelay + "ms");
		Thread.sleep(waitForSenderDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
		assertEquals("The value of the object at shutdown is incorrect", 0, obj.getValue());
		assertEquals("The request was not received", TestPermissionsClientSender, obj.requesterName);
	}

	/**
	 * Tests override of allowRequestedDeletion True
	 * The receiver (this) creates an object. The sender locates that object and sends a
	 * request for deletion of it. The receiver rejects/ignores that request.
	 */
	public void testAllowRequestOwnershipTrue() throws InterruptedException, IOException {
		// startup the receiver
		new TestPermissionsObject();
		TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
		obj.setValue(1);
		obj = null;
		
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowRequestOwnershipTrue");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForSenderDelay + "ms");
		Thread.sleep(waitForSenderDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
		assertEquals("The final owner of the object is incorrect", TestPermissionsClientSender, obj.getOwnerName());
		assertEquals("The request was not received", TestPermissionsClientSender, obj.requesterName);
	}

	/**
	 * Tests override of allowRequestedDeletion False
	 * The receiver (this) creates an object. The sender locates that object and sends a
	 * request for deletion of it. The receiver rejects/ignores that request.
	 */
	public void testAllowRequestOwnershipFalse() throws InterruptedException, IOException {
		// startup the receiver
		new TestPermissionsObject();
		TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
		obj.setValue(0);
		obj = null;
		
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowRequestOwnershipFalse");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForSenderDelay + "ms");
		Thread.sleep(waitForSenderDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
		assertEquals("The final owner of the object is incorrect", TestPermissionsClientReceiver, obj.getOwnerName());
		assertEquals("The request was not received", TestPermissionsClientSender, obj.requesterName);
	}

}

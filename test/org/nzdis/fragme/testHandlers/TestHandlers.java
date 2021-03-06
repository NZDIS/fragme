package org.nzdis.fragme.testHandlers;

import java.io.IOException;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.objects.FMeObject;
import org.nzdis.fragme.objects.NewFMeObjectObserver;
import org.nzdis.fragme.peers.PeerManagerImpl;
import org.nzdis.fragme.util.DetermineOS;

import junit.framework.TestCase;

/**
 * This class tests the handlers functionalities implemented in FMeObjects.
 * 
 * @author Nathan Lewis
 */
public class TestHandlers extends TestCase implements NewFMeObjectObserver {

	public static final String TestGroupName = "jUnitTestHandlers";
	public static final String TestHandlersOtherClient = "TestHandlersOtherClient";
	public static final String TestHandlersTester = "TestHandlersTester";
	
	static final Integer startupDelay = 5000;
	static final Integer waitForOtherClientDelay = 10000;
	static String path = "java -jar ";
	
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestClass.java as main class
		 */
		String subPath = TestHandlers.class.getResource(TestHandlersOtherClient.class.getSimpleName() + ".jar").getPath();
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
		ControlCenter.setUpConnections(TestGroupName, TestHandlersTester);
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests override of allowChange
	 * The receiver (this) creates an object. The other client finds the object and
	 * tries to modify it. The sender does not accept the update (sends a fail message and original values).
	 */
	public void testAllowChange() throws InterruptedException, IOException {
		// startup the tester
		new TestHandlersObject();
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.createNewObject(TestHandlersObject.class);
		obj.setValue(0);
		obj.change();
		obj = null;

		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowChange");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		assertEquals("The value of the object at shutdown is incorrect", 0, obj.getValue());
		assertEquals("The request was not received", "allowChange", obj.test);
		Thread.sleep(waitForOtherClientDelay);
	}

	/**
	 * Tests receiving of allowChangeFailed notification
	 * The other client creates the object. The tester sends a change. The other client
	 * rejects the change. The tester receives notification of the failure and the original values. 
	 */
	public void testAllowChangeFailed() throws InterruptedException, IOException {
		// startup the tester
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowChangeFailed");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// find the object and make the request
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		obj.value = 3;
		obj.change();
		System.out.println("sent change");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay/2 + "ms");
		Thread.sleep(waitForOtherClientDelay/2);
		System.out.println("wait for sender finished");
		
		// check the results
		assertEquals("The value of the object at shutdown is incorrect", 0, obj.getValue());
		assertEquals("The failure notification was not received", "changeFailed", obj.failed);
		Thread.sleep(waitForOtherClientDelay);
	}

	/**
	 * Tests override of allowDelegateOwnership
	 * The receiver (this) creates an object. The other client finds the object and
	 * tries to take ownership of it. The tester does not accept the update.
	 */
	public void testAllowDelegateOwnership() throws InterruptedException, IOException {
		// startup the tester
		new TestHandlersObject();
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.createNewObject(TestHandlersObject.class);
		obj.setValue(0);
		obj.change();
		obj = null;

		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowDelegateOwnership");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		assertEquals("The owner of the object at shutdown is incorrect", TestHandlersTester, obj.getOwnerName());
		assertEquals("The request was not received", "allowDelegateOwnership", obj.test);
		Thread.sleep(waitForOtherClientDelay);
	}

	/**
	 * Tests receiving of allowDelegateOwnershipFailed notification
	 * The other client creates the object. The tester requests ownership. The other client
	 * rejects the change. The tester receives notification of the failure. 
	 */
	public void testAllowDelegateOwnershipFailed() throws InterruptedException, IOException {
		// startup the tester
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowDelegateOwnershipFailed");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// find the object and make the request
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		obj.requestOwnership();
		System.out.println("sent change");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay/2 + "ms");
		Thread.sleep(waitForOtherClientDelay/2);
		System.out.println("wait for sender finished");
		
		// check the results
		assertEquals("The owner of the object at shutdown is incorrect", TestHandlersOtherClient, obj.getOwnerName());
		assertEquals("The failure notification was not received", "delegateOwnershipFailed", obj.failed);
		Thread.sleep(waitForOtherClientDelay);
	}

	/**
	 * Tests override of allowDelete
	 * The receiver (this) creates an object. The other client finds the object and
	 * tries to delete it. The tester does not accept the delete.
	 */
	public void testAllowDelete() throws InterruptedException, IOException {
		// startup the tester
		new TestHandlersObject();
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.createNewObject(TestHandlersObject.class);
		obj.setValue(0);
		obj.change();
		obj = null;

		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowDelete");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		assertEquals("The owner of the object at shutdown is incorrect", TestHandlersTester, obj.getOwnerName());
		assertEquals("The request was not received", "allowDelete", obj.test);
		Thread.sleep(waitForOtherClientDelay);
	}

	/**
	 * Tests receiving of allowDeleteFailed notification
	 * The other client creates the object. The tester tries to delete it. The other client
	 * rejects the delete. The tester receives notification of the failure. 
	 */
	public void testAllowDeleteFailed() throws InterruptedException, IOException {
		// startup the tester
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testAllowDeleteFailed");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// find the object and make the request
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		obj.delete();
		System.out.println("sent change");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay/2 + "ms");
		Thread.sleep(waitForOtherClientDelay/2);
		System.out.println("wait for sender finished");
		
		// check the results
		assertEquals("The owner of the object at shutdown is incorrect", TestHandlersOtherClient, obj.getOwnerName());
		assertEquals("The failure notification was not received", "deleteFailed", obj.failed);
		Thread.sleep(waitForOtherClientDelay);
	}

	/**
	 * Tests newFMeObjectObserver
	 * The other client creates the object. The tester tries to delete it. The other client
	 * rejects the delete. The tester receives notification of the failure. 
	 */
	String receivedNewObject = null; 
	public void testNewFMeObjectObserver() throws InterruptedException, IOException {
		// startup the tester
		System.out.println("waiting for startup " + startupDelay + "ms");
		Thread.sleep(startupDelay);
		System.out.println("wait for startup finished");
		
		ControlCenter.registerNewFMeObjectObserver(this);
		
		// startup the sender (will send based on parameter specified)
		Runtime.getRuntime().exec(path + " testNewFMeObjectObserver");
		
		// give the sender time to startup and complete it's operations)
		System.out.println("waiting for sender " + waitForOtherClientDelay + "ms");
		Thread.sleep(waitForOtherClientDelay);
		System.out.println("wait for sender finished");
		
		// check the results
		TestHandlersObject obj = (TestHandlersObject)ControlCenter.getAllObjects(TestHandlersObject.class).elementAt(0);
		assertEquals("The new object was not received", 0, obj.value);
		assertEquals("The new object notification was not received", TestHandlersOtherClient, receivedNewObject);
		Thread.sleep(waitForOtherClientDelay);
	}

	@Override
	public void newFMeObject(FMeObject object) {
		receivedNewObject = object.getOwnerName();
	}

}

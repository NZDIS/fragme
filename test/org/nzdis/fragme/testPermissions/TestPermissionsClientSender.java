package org.nzdis.fragme.testPermissions;

import org.nzdis.fragme.ControlCenter;

/**
 * Sets up a new client that sends messages - to be compiled as second instance 
 * to test message passing.
 * 
 * @author Frank Wu
 * @author Christopher Frantz - revised for automated testing
 *
 */
public class TestPermissionsClientSender {
	
	public static final String TestGroupName = "jUnitTestPermissions";
	public static final String TestPermissionsClientSender = "TestPermissionsClientSender";
	public static final String TestPermissionsClientReceiver = "TestPermissionsClientReceiver";

	private static final Integer startupDelay = 5000;
	private static final Integer shutdownDelay = 5000;
	
	/**
	 * Connects to the framework and sends messages to other peers
	 * 
	 * @param args is not used
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws InterruptedException {
		// statup 
		ControlCenter.setUpConnections(TestGroupName, TestPermissionsClientSender);
		
		// wait to allow synchronization to complete
		Thread.sleep(startupDelay);

		// determine which test to perform
		String testName = null;
		if(args != null && args.length > 0){
			testName = args[0];
		}
		if(testName != null){
			if(testName.equals("testAllowDeserializeTrue") || testName.equals("testAllowDeserializeFalse")) {
				/**
				 * allow deserialize - modify the variable
				 */
				TestPermissionsObject obj = 
						(TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
				obj.setValue(2);
				obj.change();
				System.out.println("Sent updated value for test " + testName);
			} else if(testName.equals("testAllowRequestOwnershipTrue") || testName.equals("testAllowRequestOwnershipFalse")) {
				/**
				 * allow delegate ownership - send a request
				 */
				TestPermissionsObject obj = 
						(TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
				obj.requestOwnership();
				System.out.println("Sent request for ownership for test " + testName);
			} else if(testName.equals("testAllowDelegateOwnershipTrue") || testName.equals("testAllowDelegateOwnershipFalse")) {
				/**
				 * allow delete - send a request
				 */
				TestPermissionsObject obj = 
						(TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
				obj.delete();
				System.out.println("Sent delete for test " + testName);
			} else {
				System.out.println("Test " + testName + " is unknown - doing nothing");
			}
		} else {
			System.out.println("No valid test parameter specified");
		}
		Thread.sleep(shutdownDelay/2);
		Thread.sleep(shutdownDelay/2);
		ControlCenter.closeUpConnections();
	}
}

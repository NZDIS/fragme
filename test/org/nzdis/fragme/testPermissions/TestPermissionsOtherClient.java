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
public class TestPermissionsOtherClient {
	
	public static final String TestGroupName = "jUnitTestPermissions";
	public static final String TestPermissionsOtherClient = "TestPermissionsOtherClient";
	public static final String TestPermissionsTester = "TestPermissionsTester";

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
		ControlCenter.setUpConnections(TestGroupName, TestPermissionsOtherClient);
		
		// wait to allow synchronization to complete
		Thread.sleep(startupDelay);

		// determine which test to perform
		String testName = null;
		if(args != null && args.length > 0){
			testName = args[0];
		}
		if(testName != null){
			if(testName.equals("testAllowChange")) {
				/**
				 * allow change - will fail
				 */
				// Find the object
				TestPermissionsObject obj = 
						(TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
				// Try to change it
				obj.setValue(2);
				obj.change();
				System.out.println("Done test " + testName);
			} else if(testName.equals("testAllowChangeFailed")) {
				/**
				 * allow change failed - will fail
				 */
				// Create the object for the testers to use
				new TestPermissionsObject();
				TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
				obj.setValue(0);
				obj.change();
				System.out.println("Done test " + testName);
				Thread.sleep(shutdownDelay);
			} else if(testName.equals("testAllowDelegateOwnership")) {
				/**
				 * allow delegate ownership - will fail
				 */
				// Find the object
				TestPermissionsObject obj = 
						(TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
				// Try to take ownership of it
				obj.requestOwnership();
				System.out.println("Done test " + testName);
			} else if(testName.equals("testAllowDelegateOwnershipFailed")) {
				/**
				 * allow delegate ownership - will fail
				 */
				// Create the object for the testers to use
				new TestPermissionsObject();
				TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
				obj.setValue(0);
				obj.change();
				System.out.println("Done test " + testName);
				Thread.sleep(shutdownDelay);
			} else if(testName.equals("testAllowDelete")) {
				/**
				 * allow delete - will fail
				 */
				// Find the object
				TestPermissionsObject obj = 
						(TestPermissionsObject)ControlCenter.getAllObjects(TestPermissionsObject.class).elementAt(0);
				// Try to delete of it
				obj.delete();
				System.out.println("Done test " + testName);
			} else if(testName.equals("testAllowDeleteFailed")) {
				/**
				 * allow delete - will fail
				 */
				// Create the object for the testers to use
				new TestPermissionsObject();
				TestPermissionsObject obj = (TestPermissionsObject)ControlCenter.createNewObject(TestPermissionsObject.class);
				obj.setValue(0);
				obj.change();
				System.out.println("Done test " + testName);
				Thread.sleep(shutdownDelay);
			} else {
				System.out.println("Test " + testName + " is unknown - doing nothing");
			}
		} else {
			System.out.println("No valid test parameter specified");
		}
		Thread.sleep(shutdownDelay);
		ControlCenter.closeUpConnections();
	}
}

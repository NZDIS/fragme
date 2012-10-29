package org.nzdis.fragme.testOwnershipTransfer;

import org.nzdis.fragme.ControlCenter;

/**
 * Sets up a new client that sends messages - to be compiled as second instance 
 * to test message passing.
 * 
 * @author Frank Wu
 * @author Christopher Frantz - revised for automated testing
 *
 */
public class TestOwnershipOtherClient {
	
	public static final String TestGroupName = "jUnitTestOwnershipTransfer";
	public static final String TestOwnershipOtherClient = "TestOwnershipOtherClient";
	public static final String TestOwnershipTester = "TestOwnershipTester";

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
		ControlCenter.setUpConnections(TestGroupName, TestOwnershipOtherClient);
		
		// wait to allow synchronization to complete
		Thread.sleep(startupDelay);

		// determine which test to perform
		String testName = null;
		if(args != null && args.length > 0){
			testName = args[0];
		}
		if(testName != null){
			if(testName.equals("testRequestOwnership")) {
				/**
				 * request ownership
				 */
				TestOwnershipObject obj = 
						(TestOwnershipObject)ControlCenter.getAllObjects(TestOwnershipObject.class).elementAt(0);
				obj.requestOwnership();
				System.out.println("Sent request for ownership for test " + testName);
			} else if(testName.equals("testDelegateOwnership1")) {
				/**
				 * delegate ownership 1
				 */
				new TestOwnershipObject();
				TestOwnershipObject obj = (TestOwnershipObject)ControlCenter.createNewObject(TestOwnershipObject.class);
				obj.delegateOwnership(TestOwnershipTester);
				System.out.println("Delegated object for test " + testName);
			} else if(testName.equals("testDelegateOwnership2")) {
				/**
				 * delegate ownership 2
				 */
				System.out.println("Waiting for object to be delegated to us for test " + testName);
				Thread.sleep(startupDelay);
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

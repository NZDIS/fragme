package org.nzdis.fragme;

import junit.framework.TestSuite;
import org.nzdis.fragme.testControlCenter.TestControlCenter;
import org.nzdis.fragme.testControlCenter.TestGetObjects;
import org.nzdis.fragme.testFragMeFactory.TestFragMeFactory;
import org.nzdis.fragme.testMessageSending.TestMessageSending;
import org.nzdis.fragme.testObjects.TestFMeObject;
import org.nzdis.fragme.testObjects.TestObjectManager;
import org.nzdis.fragme.testOwnershipTransfer.TestOwnershipTransfer;
import org.nzdis.fragme.testPermissions.TestPermissions;

/**
 * This class creates a test suite that runs the individual unit test cases for
 * the FragMe framework
 * 
 * @author Frank Wu
 * @author Christopher Frantz - revised (05/09/2012)
 * 
 */
public class FragMeTestSuite extends TestSuite {
	
	static{
		//System.setProperty("java.net.preferIPv4Stack", "true");
    	//System.setProperty("java.net.preferIPv6Addresses", "false");
	}
	
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(TestFMeObject.class);
		suite.addTestSuite(TestFragMeFactory.class);
		suite.addTestSuite(TestObjectManager.class);
		suite.addTestSuite(TestGetObjects.class);
		suite.addTestSuite(TestMessageSending.class);
		suite.addTestSuite(TestControlCenter.class);
		suite.addTestSuite(TestOwnershipTransfer.class);
		suite.addTestSuite(TestPermissions.class);
		return suite;
	}

}

package org.nzdis.fragme.testObjects;

import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.exceptions.StartUpException;
import org.nzdis.fragme.objects.ObjectManagerImpl;
import junit.framework.TestCase;

/**
 * <<<<<<< TestObjectManager.java Tests ObjectManagerImpl methods - as
 * most of these are called via PeerManager, ControlCenter, and FragMeFactory,
 * the majority of tests are performed in these classes. ======= Tests
 * ObjectManagerImpl methods - as most of these are called via PeerManager,
 * ControlCenter, and FragMeFactory, the majority of tests are already performed
 * in the other associated test classes >>>>>>> 1.2
 * 
 * @author Morgan Bruce
 * 
 */
public class TestObjectManager extends TestCase {

	/**
	 * Sets up for each test
	 */
	public void setUp() {
		ControlCenter.setUpConnections("testGroup1", "testPeer");
	}

	/**
	 * Tears down after each test
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests that the ObjectManager is initialised correctly
	 * 
	 * @throws StartUpException
	 */
	public void testGetInstance() throws StartUpException {
		assertNotNull("ObjectManagerImpl.getInstance() should not return null",
				ObjectManagerImpl.getInstance());
	}

}

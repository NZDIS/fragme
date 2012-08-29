package org.nzdis.fragme.testObjects;

import java.util.Vector;
import org.nzdis.fragme.*;
import org.nzdis.fragme.testControlCenter.TestObjectC;
import junit.framework.TestCase;

/**
 * Tests features of the FMeObject class.
 * 
 * @author Morgan Bruce
 * 
 */
public class TestFMeObject extends TestCase {

	private TestObjectC test;

	/**
	 * Sets up before each test
	 */
	public void setUp() {
		ControlCenter.setUpConnections("testGroup1", "testPeer2");
		test = (TestObjectC) ControlCenter.createNewObject(TestObjectC.class);
	}

	/**
	 * Tears down after each test
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	/**
	 * Tests the two change() methods of FMeObject.
	 * 
	 * Currently only tests the local occurence; a second JVM needs to be
	 * started to correctly test the propagation of changes across peers.
	 * 
	 */
	public void testChange() {
		test.setDummy("Some text to test");
		test.change();
		assertEquals("The text set is incorrect", "Some text to test", test
				.getDummy());

		test.setTest(12);
		test.change("test");
		assertEquals("The value changed to the field \"test\" is incorrect",
				12, test.getTest());
	}

	/**
	 * Tests deleting a FMeObject
	 * 
	 * Currently only tests the local occurence; propagation of deletes requires
	 * a second JVM to test correctly, or a specialised testing framework.
	 * 
	 */
	public void testDelete() {
		test.delete();
		Vector x = ControlCenter.getOwnObjects();
		assertFalse("The object has not been deleted", x.contains(test));
		test = null; // must be performed after FragMe delete!
	}

	/**
	 * Tests that the FMeObject has stored the correct owner address, as in the
	 * ControlCenter
	 * 
	 */
	public void testGetOwnerAddr() {
		assertEquals(
				"The owner address of the object should be the same as my address",
				ControlCenter.getMyAddress(), test.getOwnerAddr());
	}

	/**
	 * Tests that the equals method works as expected
	 * 
	 */
	public void testEquals() {
		TestObjectC test2 = (TestObjectC) ControlCenter
				.createNewObject(TestObjectC.class);
		// trival case
		assertTrue(
				"equal() method should return true as it is comparing same object",
				test.equals(test));
		// non-trivial case
		assertFalse(
				"equal() method should return false ass it is comparing different objects",
				test.equals(test2));
	}

}

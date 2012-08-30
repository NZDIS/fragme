package org.nzdis.fragme.testControlCenter;

import java.io.Serializable;

import org.nzdis.fragme.ControlCenter;

/**
 * Used by getObjects tests to provide a simple second peer,
 * in order to test object management functionality.  This 
 * class is compiled as TestGetObjectPeer.jar
 * 
 * @author Morgan Bruce
 *
 */
public class TestGetObjectsPeer implements Serializable {
	public static void main(String args[]) {
		new TestObjectC();
		ControlCenter.setUpConnections("testGroup1", "testPeer2");
		TestObjectC c = (TestObjectC) ControlCenter.createNewObject(TestObjectC.class);
		c.setDummy("hello2");
		c.change();
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ControlCenter.closeUpConnections();
		System.exit(0);
	}
}

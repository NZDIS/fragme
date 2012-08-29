package org.nzdis.fragme.testControlCenter;

import java.io.Serializable;
import org.nzdis.fragme.ControlCenter;

public class TestClass implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	public String test = "a";
	public int testInt = 1;

	/**
	 * Sets up a new connection for the peer testPeer2 to join group testGroup1
	 * 
	 * @param args is not used
	 */
	public static void main(String args[]) {
		ControlCenter.setUpConnections("testGroup1", "testPeer2");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ControlCenter.closeUpConnections();
		System.exit(0);
	}
}

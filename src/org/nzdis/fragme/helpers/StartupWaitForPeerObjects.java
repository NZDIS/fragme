package org.nzdis.fragme.helpers;

import org.nzdis.fragme.ControlCenter;

public class StartupWaitForPeerObjects implements StartupWaitHelper {

	int objectsPerPeer = 0;
	
	public StartupWaitForPeerObjects(int objectsPerPeer) {
		this.objectsPerPeer = objectsPerPeer;
	}
	
	@Override
	public void waitForCondition() {
		while(ControlCenter.getAllObjects().size() < ControlCenter.getNoOfPeers() * objectsPerPeer) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

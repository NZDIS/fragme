package org.nzdis.fragme.helpers;

import org.nzdis.fragme.ControlCenter;

/**
 * 
 * @author ndlewis
 *
 */
public class StartupWaitForObjects implements StartupWaitHelper {

	int objectsRequired = 0;
	
	public StartupWaitForObjects(int objectsRequired) {
		this.objectsRequired = objectsRequired;
	}
	
	@Override
	public void waitForCondition() {
		if (ControlCenter.getNoOfPeers() > 0) {
			while(ControlCenter.getAllObjects().size() < objectsRequired) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

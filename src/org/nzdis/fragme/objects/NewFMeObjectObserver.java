package org.nzdis.fragme.objects;

public interface NewFMeObjectObserver extends FMeObserver {

	/**
	 * Called when an object has been received by the peer manager (only object from other peers)
	 * @param object The object that has been added
	 */
	void newFMeObject(FMeObject object);
	
}

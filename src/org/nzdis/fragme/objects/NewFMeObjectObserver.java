package org.nzdis.fragme.objects;

public interface NewFMeObjectObserver extends FMeObserver {

	/**
	 * Called when an object has been added to the object manager
	 * @param object The object that has been added
	 */
	void newFMeObject(FMeObject object);
	
}

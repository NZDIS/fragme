package org.nzdis.fragme.objects;

public interface ChangeObserver extends FMeObserver {

	/**
	 * Called when an object has changed
	 * @param object The object that has changed
	 */
	void changed(FMeObject object);
	
	/**
	 * Called when ownership of an object has been delegated from one peer to another
	 * @param object The object whose ownership has been delegated
	 */
	void delegatedOwnership(FMeObject object);
	
	/**
	 * Called when an object is about to be deleted
	 * @param object The object that is about to be deleted
	 */
	void deleted(FMeObject object);
	
}

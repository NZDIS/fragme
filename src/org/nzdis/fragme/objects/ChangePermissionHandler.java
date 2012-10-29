package org.nzdis.fragme.objects;

public interface ChangePermissionHandler extends FMeObserver {

	/**
	 * Called when a change to an owned object is made by another peer
	 * @param originalObject FMeObject that is being changed 
	 * @param newObject FMeObject containing values that will replace those that are in the original object
	 * @param changeRequester Name of the peer that is making the change
	 * @return Whether or not the change should be allowed
	 *           - if this method is not applicable to your application then you should return TRUE
	 */
	boolean allowChange(FMeObject originalObject, FMeObject newObject, String changeRequester);
	
	/**
	 * Called when a change to an owned object's field is made by another peer
	 * @param originalObject FMeObject that is being changed 
	 * @param objectReflection FMeObjectReflection containing field name and value that will replace the value in the original object
	 * @param changeRequester Name of the peer that is making the change
	 * @return Whether or not the change should be allowed
	 *           - if this method is not applicable to your application then you should return TRUE
	 */
	boolean allowChangeField(FMeObject originalObject, FMeObjectReflection objectReflection, String changeRequester);
	
	/**
	 * Called when a change to an un-owned FMeObject has failed
	 * @param object FMeObject that was unsuccessfully changed (containing  values)
	 */
	void changeFailed(FMeObject object);
	
}

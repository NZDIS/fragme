package org.nzdis.fragme.objects;

public interface DelegateOwnershipPermissionHandler extends FMeObserver {

	boolean allowDelegateOwnership(FMeObject object, String delegateRequester);
	
	void delegateOwnershipFailed(FMeObject object);
	
}

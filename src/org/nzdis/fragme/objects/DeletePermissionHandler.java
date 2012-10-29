package org.nzdis.fragme.objects;

public interface DeletePermissionHandler extends FMeObserver {

	boolean allowDelete(FMeObject object, String deleteRequester);
	
	void deleteFailed(FMeObject object);
	
}

package org.nzdis.fragme.testPermissions;

import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.ChangePermissionHandler;
import org.nzdis.fragme.objects.DelegateOwnershipPermissionHandler;
import org.nzdis.fragme.objects.DeletePermissionHandler;
import org.nzdis.fragme.objects.FMeObject;
import org.nzdis.fragme.objects.FMeObjectReflection;

public class TestPermissionsObject extends FMeObject implements ChangePermissionHandler, DelegateOwnershipPermissionHandler, DeletePermissionHandler {
	private static final long serialVersionUID = -3973694673281775829L;

	public String requesterName = null;
	public int value = 1;
	public String test = null;
	public String failed = null;
	public String success = null;
	
	@Override
	public void deserialize(FMeObject serObject) {
		this.value = ((TestPermissionsObject)serObject).getValue();
	}

	private static class Factory extends FragMeFactory {
		protected FactoryObject create() {
			return new TestPermissionsObject();
		}
	}

	static {
		FragMeFactory.addFactory(new Factory(), TestPermissionsObject.class);
	}
	
	public TestPermissionsObject() {
		this.register(this);
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(int v) {
		this.value = v;
	}

	@Override
	public boolean allowDelete(FMeObject object, String deleteRequester) {
		this.requesterName = deleteRequester;
		this.test = "allowDelete";
		return false;
	}

	@Override
	public void deleteFailed(FMeObject object) {
		// TODO
		this.failed = "deleteFailed";
	}

	@Override
	public boolean allowDelegateOwnership(FMeObject object, String delegateRequester) {
		this.requesterName = delegateRequester;
		this.test = "allowDelegateOwnership";
		return false;
	}

	@Override
	public void delegateOwnershipFailed(FMeObject object) {
		this.failed = "delegateOwnershipFailed";
	}

	@Override
	public boolean allowChange(FMeObject originalObject, FMeObject newObject, String changeRequester) {
		this.requesterName = changeRequester;
		this.test = "allowChange";
		return false;
	}

	@Override
	public boolean allowChangeField(FMeObject originalObject, FMeObjectReflection objectReflection, String changeRequester) {
		// TODO Auto-generated method stub
		this.test = "allowChangeField";
		return false;
	}

	@Override
	public void changeFailed(FMeObject object) {
		// TODO Auto-generated method stub
		this.failed = "changeFailed";
	}

	@Override
	public void changed(FMeObject object) {
		// TODO Auto-generated method stub
		this.success = "changed";
	}

	@Override
	public void delegatedOwnership(FMeObject object) {
		// TODO Auto-generated method stub
		this.success = "delegatedOwnership";
	}

	@Override
	public void deleted(FMeObject object) {
		// TODO Auto-generated method stub
		this.success = "deleted";
	}
	
}

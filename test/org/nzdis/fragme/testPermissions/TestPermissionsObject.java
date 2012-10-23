package org.nzdis.fragme.testPermissions;

import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;

public class TestPermissionsObject extends FMeObject {
	private static final long serialVersionUID = -3973694673281775829L;

	public String requesterName = null;
	public int value = 1; 
	
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
	
	@Override
	public boolean allowDeserialize(String requesterName) {
		this.requesterName = requesterName;
		if (value == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean allowDelegationOfOwnership(String requesterName) {
		this.requesterName = requesterName;
		if (value == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean allowDelete(String requesterName) {
		this.requesterName = requesterName;
		if (value == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void changedObject() {
	}

	@Override
	public void deletedObject() {
	}

	public int getValue() {
		return this.value;
	}
	
	public void setValue(int v) {
		this.value = v;
	}
	
}

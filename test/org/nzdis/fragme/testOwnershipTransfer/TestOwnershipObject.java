package org.nzdis.fragme.testOwnershipTransfer;

import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;

public class TestOwnershipObject extends FMeObject {
	private static final long serialVersionUID = -3973694673281775829L;

	@Override
	public void deserialize(FMeObject serObject) {
	}

	private static class Factory extends FragMeFactory {
		protected FactoryObject create() {
			return new TestOwnershipObject();
		}
	}

	static {
		FragMeFactory.addFactory(new Factory(), TestOwnershipObject.class);
	}

	@Override
	public void changedObject() {
	}

	@Override
	public void deletedObject() {
	}
}

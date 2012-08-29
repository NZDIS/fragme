package org.nzdis.fragme.testControlCenter;

import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;

/**
 * An object containing dummy data: used in tests of
 * object management functionality
 * 
 * @author Morgan Bruce
 *
 */
public class TestObjectA extends FMeObject {

	protected String dummy;
	
//	public TestObjectA() {}
	
	public void changedObject() {
		System.out.println("Received a change notification!");
		this.setChanged();
		this.notifyObservers();
	}

	public void deletedObject() {
		System.out.println("Received a delete notification!");
		this.setChanged();
		this.notifyObservers();
	}

	private static class Factory extends FragMeFactory {

		protected FactoryObject create() {
			return new TestObjectA();
		}
		
	}

	static {
		FragMeFactory.addFactory(new Factory(), TestObjectA.class);
	}

	public void deserialize(FMeObject serObject) {
		this.dummy = ((TestObjectA) serObject).getDummy();
	}

	/**
	 * @return the dummy
	 */
	public String getDummy() {
		return this.dummy;
	}

	/**
	 * @param dummy the dummy to set
	 */
	public void setDummy(String dummy) {
		this.dummy = dummy;
	}

}

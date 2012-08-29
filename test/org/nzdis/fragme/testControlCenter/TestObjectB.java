package org.nzdis.fragme.testControlCenter;

import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;

/**
 * An object containing additional dummy data - like
 * its superclass, used in tests of object management
 * 
 * @author Morgan Bruce
 *
 */
public class TestObjectB extends TestObjectA {

//	private String dummy;
//	
//	public TestObjectB() {}
//	
//	public void changedObject() {
//		System.out.println("Received a change notification!");
//
//		this.setChanged();
//		this.notifyObservers();
//	}
//
//	public void deletedObject() {
//		System.out.println("Received a delete notification!");
//
//		this.setChanged();
//		this.notifyObservers();
//	}

	private static class Factory extends FragMeFactory {

		protected FactoryObject create() {
			return new TestObjectB();
		}
		
	}

	static {
		FragMeFactory.addFactory(new Factory(), TestObjectB.class);
	}

//	public void deserialize(FMeObject serObject) {
//		this.dummy = ((TestObjectB) serObject).getDummy();
//	}
//
//	/**
//	 * @return the dummy
//	 */
//	public String getDummy() {
//		return this.dummy;
//	}
//
//	/**
//	 * @param dummy the dummy to set
//	 */
//	public void setDummy(String dummy) {
//		this.dummy = dummy;
//	}

}

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
public class TestObjectC extends TestObjectA {
//
//	public String dummy;
	public int test;
	
	public int getTest() {
		return test;
	}

	public void setTest(int test) {
		this.test = test;
	}
//
//	public TestObjectC() {}
//		
//	public String getDummy() {
//		return dummy;
//	}
//
//	public void setDummy(String dummy) {
//		this.dummy = dummy;
//	}
//
//	public void changedObject() {
//		System.out.println("Received a change notification!");
//		this.setChanged();
//		this.notifyObservers();
//	}
//		
//	public void deletedObject() {
//		System.out.println("Received a delete notification!");
//		this.setChanged();
//		this.notifyObservers();
//	}
//
//	public void deserialize(FMeObject serObject) {
//		this.dummy = ((TestObjectC) serObject).getDummy();
//		this.test = ((TestObjectC) serObject).getTest();
//	}
		
	private static class Factory extends FragMeFactory {
		protected FactoryObject create() {
			return new TestObjectC();
		}
	}
	
	static {
		FragMeFactory.addFactory(new Factory(), TestObjectC.class);
	}
		
}


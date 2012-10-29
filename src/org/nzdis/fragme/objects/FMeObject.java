package org.nzdis.fragme.objects;

import java.io.Serializable;
import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.ControlCenter;
import org.jgroups.Address;

/**
 * All shared objects within the system must extend the FMeObject class.
 *  
 * Each implementation of FMeObject should only be instanced by the FragMeFactory Class. 
 * Therefore its constructor MUST be private, but can accept arbitrary parameter. Also 
 * it must contain a static inner class, which extends FragMeFactory and a static constructor.
 * The static constructor adds the static implementation of FragMeFactory to the FragMeFactory Class.
 *
 * This sounds complicated but the following example shows that it is indeed very easy to implement. 
 * (The example only shows the relevant methods of the FMeObject implementation):
 *
 * Revision 1.7: FRAGMeObject is now a subclass of java.util.Observable. This allows 
 * the application developer, to use the Observer-design-pattern 
 * (@see http://www.research.ibm.com/designpatterns/example.htm ).
 *
 * <code>
 * public class BlaBlaObject extends FMeObject {
 *     // the private constructor
 *     private BlaBlaObject(...) {
 *        ...
 *     }
 *
 *     // the static inner class
 *     private static class Factory extends FragMeFactory {
 *        protected Factorizable create() {
 *          return new BlaBlaObject(...);
 *        }
 *     }
 *
 *     // the static constructor
 *     static () {
 *       Name="...";
 *       SerializedObjectName = "...";
 *       FragMeFactory.addFactory( new Factory(), Name );
 *     }
 * }
 * </code>
 *
 * FMeObject.java<br>
 * Created: 15/08/2003  11:40:24<br>
 * Modified: 20/08/2003  10:54:40 (MEZ +1h)<br>
 *
 * @author  <a href="mailto:benjamin.herrmann@in.tum.de">Benjamin Herrmann</a>
 * @version $Revision: 1.1 $Date: 2008/09/03 04:24:49 $
 * @refactored Morgan Bruce 16/07/2008
 */
public abstract class FMeObject extends FMeObservable implements FactoryObject, Serializable, ChangeObserver {
	private transient static final long serialVersionUID = 6672421720523501646L;

	/**
	 * The unique id of this FMeObject
	 */
	private String id;

	/**
	 * The address of the owner peer of this FMeObject
	 */
	private Address ownerAddr = null;
	
	/** constant for owner address field to enforce consistent use for delegation -- NEEDS to match name of field*/
	transient private static final String OWNER_ADDRESS_FIELD = "ownerAddr";
	// TODO 
	// At minimum, replace ownerAddr with ownerName
	// Better yet, remove owner from FMeObject - it can be found in OM object containers 
	

	/** 
	 * If the reflection framework is changing an object 
	 * then this will be set to true
	 * 
	 * @see "2005FragmeDoc.pdf"
	 */
	protected boolean frameWorkChanging = false;

	public FMeObject() {}

	/**
	 * Sets the framework changing state
	 * 
	 * @param changing boolean framework state to set
	 */
	public void frameworkChanging(boolean changing) {
		this.frameWorkChanging = changing;
	}

	/**
	 * Any subclass of FMeObject calls this method if it is changed.
	 * This method then calls the ObjectManager to handle the change in an
	 * appropriate way: distribute if this peer is the owner or ask the owner
	 * if this peer is not the owner
	 */
	public final void change() {
		ControlCenter.getObjectManager().pushChange(this);
	}

	/**
	 * Similar to pushChange(), calls the ObjectManager to handle the change of
	 * a particular field within an object
	 * 
	 * @param fieldName name of field changed within object
	 * @see org.globalse.fragme.objects.FMeObject#pushChange()
	 */
	public final void change(String fieldName) {
		if (!frameWorkChanging) {
			ControlCenter.getObjectManager().pushChange(this, fieldName);
		}
	}

	/**
	 * Deletes this object from object storage locally if it belongs to this peer, 
	 * broadcast the deletion to other peers, otherwise requests a delete from 
	 * the owner of the object
	 * 
	 * Note: the reference to this object must also be set to null for garbage
	 * collection to occur
	 */
	public final void delete() {
		Address myAddress = ControlCenter.getMyAddress();
		if (myAddress == this.ownerAddr) {
			ControlCenter.getObjectManager().deleteObject(this.ownerAddr,
					this.id);
		} else {
			ControlCenter.getObjectManager().requestDeleteObject(
					this.ownerAddr, this.id);
		}
	}

	/**
	 * Deserializes an FMeSerialised object - basically, gets
	 * each field value and sets the local object's fields
	 * 
	 * Example implementation:
	 * <code>
	 * public void deserialize(FMeObject serObject) {
	 *		this.name = ((BlahBlahObject) serObject).getName();
	 * }
	 * </code>
	 * 
	 * @param serObject serialized FMeObject to deserialize
	 */
	public abstract void deserialize(FMeObject serObject);
	
	/**
	 * This method returns the ID of the object
	 * 
	 * @return int the ID of the object.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the object's id.
	 * @param id id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the owner's address
	 * Don't call this from your application - it is called from within FragMe's systems
	 * (Calling this would give you an inconsistent world view, but would not affect other peers)
	 * @param ownerAddr the owner address to set
	 */
	public final void setOwnerAddr(Address ownerAddr) {
		// TODO verify with OM containers before changing this value
		// Or even better, don't have an owner in FMeObject - instead do a lookup from OM containers
		this.ownerAddr = ownerAddr;
	}

	/**
	 * Check if deserialization should be performed
	 * This method is called when an update to an owned object is received
	 * Is only called if this peer owns this object (you don't need to check this yourself)
	 * The default method always returns true - any update is accepted
	 * Override this method to implement different behavior
	 */
	//public boolean allowDeserialize(String requesterName) {
	//	return true;
	//}

	/**
	 * Send a request to the owner, asking them to delegate ownership to this peer
	 */
	public final void requestOwnership() {
		if (!this.ownerAddr.equals(ControlCenter.getMyAddress())) {
			ControlCenter.getObjectManager().requestOwnership(this.ownerAddr, this.id);
		}
	}

	/**
	 * Check if delegation of this object to another peer is allowed
	 * This method is called when a valid request for ownership is received
	 * Is only called if this peer owns this object (you don't need to check this yourself)
	 * The default method always returns true - any valid request for ownership is granted
	 * Override this method to implement different behavior
	 */
	//public boolean allowDelegationOfOwnership(String requesterName) {
	//	return true;
	//}

	/**
	 * Delegate ownership of this object to another peer
	 * This is automatically called when a request for ownership is received 
	 * and a call to allowDelegationOfOwnership has returned true, but can
	 * also be called by the object's owner 
	 */
	public final void delegateOwnership(String newOwnerName) {
		Address myAddr = ControlCenter.getMyAddress();
		if (this.ownerAddr.equals(myAddr)) {
			if (newOwnerName.equals(ControlCenter.getMyName())) {
				return;
			}
			Address newOwnerAddr = ControlCenter.getPeerAddress(newOwnerName);
			ControlCenter.getObjectManager().delegatedOwnership(newOwnerAddr, this);
			ControlCenter.getObjectManager().sendDelegatedOwnership(myAddr, new FMeObjectReflection(OWNER_ADDRESS_FIELD, newOwnerAddr, id));
		} else {
			throw new RuntimeException("Error: Transfer of object ownership only allowed for owned objects");
		}
	}

	/**
	 * Check if deletion of this (owned) object, requested by another peer, is allowed
	 * This method is called when a valid request for deletion is received
	 * Is only called if this peer owns this object (you don't need to check this yourself)
	 * The default method always returns true - any request for deletion is granted
	 * Override this method to implement different behavior
	 */
	//public boolean allowDelete(String requesterName) {
	//	return true;
	//}

	/**
	 * Returns the owner's address
	 * @return the owner's address
	 */
	public Address getOwnerAddr() {
		return ownerAddr;
	}

	/**
	 * Returns the owner's name
	 * @return the owner's name
	 */
	public String getOwnerName() {
		return ControlCenter.getPeerName(ownerAddr);
	}

	/**
	 * Prints the FMeObject in the String format:
	 * [Class name] Owner Address: [Owner's Address] ID: [Object ID]
	 * 
	 * @return a string consisting of class name, owner adress and object id
	 */
	public String toString() {
		return this.getClass().getName() + " Owner Address: " + this.ownerAddr
				+ " ID: " + this.id;
	}

	/**
	 * Compares two FMeObjects
	 * 
	 * @return false if different, true if the same
	 */
	public final boolean equals(Object obj) {
		try {
			FMeObject fmeObj = (FMeObject) obj;
			if (fmeObj.getId().equals(this.id)) return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("(FMeSerialized::equals()): Two objects of different type have been compared!");
		}
		return false;
	}
}

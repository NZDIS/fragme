package org.nzdis.fragme;

import java.io.IOException;
import java.util.Vector;
import org.nzdis.fragme.objects.FMeObject;
import org.jgroups.Address;

/**
 * Interface for the FragMe ObjectManager
 * 
 * @author Mengqiu Wang, Heiko Wolf
 * @refactored Morgan Bruce 16/07/2008
 */
public interface ObjectManager {

	/**
	 * This method retrieves all objects within the network at any given time
	 * 
	 * @return vector containing all the objects within the network
	 */
	public abstract Vector getAllObjects();

	/**
	 * This method gets all objects in the network at any point of time of a
	 * particular Class type
	 * 
	 * @return vector containing all the objects of a particular class type
	 * @param className
	 *            the class type to retrieve objects of
	 */
	public abstract Vector getAllObjects(Class className);

	/**
	 * This method gets all objects in the network that belong to the calling
	 * application, at any point of time
	 * 
	 * @return vector containing all objects belonging to the calling
	 *         application
	 */
	public abstract Vector getOwnObjects();

	/**
	 * This method is used by the application to get all objects in the network
	 * of its own, at any point of time of a particular Class type
	 * 
	 * @param className
	 *            the class type to retrieve objects of
	 * @return vector contain own objects of specified class type
	 */
	public abstract Vector getOwnObjects(Class className);

	/**
	 * This method is used to set the ObjectManager's address. It is called by
	 * the PeerManager.
	 * 
	 * @param addr
	 *            the FragMe/JGroups address to set
	 */
	public abstract void setMyAddress(Address addr);

	/**
	 * This method retrieves the ObjectManager's address. This is called by the
	 * PeerManager
	 * 
	 * @return address of this peer
	 */
	public abstract Address getMyAddress();

	/**
	 * This method checks if the object is owned object. If it is, calls
	 * peerManager's send with a MODIFY message and "null" as receiver address
	 * (which means it will broadcast this change to all other peers).
	 * 
	 * If it's not, calls peerManager's send with a MODIFY message and the
	 * object owner's address, which will send it as unicast to this the owner's
	 * peermanager, and make the owner broadcast the change(but not to this peer
	 * again--we can do this by checking Message's getSrc().
	 * 
	 * @param object
	 *            the FMeObject to change
	 */
	public abstract void pushChange(FMeObject object);

	/**
	 * Provides pushChange functionality for the reflection occurence; that is,
	 * when a particular field of the object is changed
	 * 
	 * @param object
	 *            the FMeObject to change
	 * @param fieldName
	 *            the name of the field being change
	 */
	public abstract void pushChange(FMeObject object, String fieldName);

	/**
	 * This method is called by the FragMeFactory to notify the ObjectManager
	 * that a new object has been created.
	 * 
	 * @param object
	 *            the new FMeObject that has been created
	 */
	public abstract void addOwnObject(FMeObject object);

	/**
	 * This method is called by the PeerManager when a change is received. It
	 * checks the object storage of a particular peer,and changes it
	 * accordingly, which means - if doesn't exist, add it, if already exists,
	 * update it.
	 * 
	 * @param object
	 *            the received changed object
	 */
	public abstract void receiveChange(FMeObject object);

	/**
	 * This method adds an object into the storage of the OM, for both own
	 * objects and other peer's objects
	 * 
	 * @param object
	 *            the object to be added into storage
	 */
	public abstract void addObject(FMeObject object);

	/**
	 * Finds any existing object that matches with the argument on condition
	 * that FMeObject's equals method returns true
	 * 
	 * @param object
	 *            the object to match
	 * @return matching object
	 */
	public abstract FMeObject lookup(FMeObject object);

	/**
	 * This method finds any existing id based on an integer id, used in the
	 * reflection (single field update) case
	 * 
	 * @param id
	 *            the id to lookup
	 * @return matching object
	 */
	public abstract FMeObject lookupById(int id);

	/**
	 * This method is used to communicate with PeerManager when a peer joins to
	 * allocate object storage for that peer
	 * 
	 * @param addr
	 *            address of peer to allocate object storage
	 */
	public abstract void allocateSpaceForPeer(Address addr);

	/**
	 * This method is called by the PeerManager to ask the ObjectManager to send
	 * its objects to a newly joined peer
	 * 
	 * @param addr
	 *            address of peer to send objects to
	 */
	public abstract void sendObjectsToNewPeer(Address addr);

	/**
	 * This methods is used to communicate with PeerManager after a peer left
	 * which should delete the object storage for that peer
	 * 
	 * @param addr
	 *            address of the peer whose objects are to be deleted
	 */
	public abstract void deletePeer(Address addr);

	/**
	 * This method deletes an object from object storage, and broadcasts the
	 * deletion
	 * 
	 * @param addr
	 *            Address of object owner
	 * @param id
	 *            the id of the object to delete
	 */
	public abstract void deleteObject(Address ownerAddr, int id);

	/**
	 * This method sends a request to the owner of the object to request the
	 * owner to delete it
	 * 
	 * @param addr
	 *            Address of object owner
	 * @param id
	 *            the id of the object to delete
	 */
	public abstract void requestDeleteObject(Address ownerAddr, int id);

	/**
	 * This method is called when a peer that the current peer is fostering
	 * drops out, it will handle the peer's objects properly, delegate the
	 * Transferables, and delete the others
	 * 
	 * @param addr
	 *            Address of peer to foster
	 */
	public abstract void delegatePeerObjects(Address addr);

	/**
	 * This method serializes an object to disk
	 * 
	 * @param object
	 *            Object
	 * @throws IOException
	 */
	public abstract void serializeToDisk(Object object) throws IOException;

	/**
	 * This method serializes an object to disk
	 * 
	 * @param String
	 *            filename Name of the file to serialize objects to
	 * @param object
	 *            Object
	 * @throws IOException
	 */
	public abstract void serializeToDisk(Object object, String fileName)
			throws IOException;

	/**
	 * This method serializes objects of particular class from disk
	 * 
	 * @param object
	 *            Object
	 * @return vector containing objects serialized from disk
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract Vector serializeFromDisk(Class className)
			throws IOException, ClassNotFoundException;

	/**
	 * This method serializes objects of particular class from disk
	 * 
	 * @param object
	 *            Object
	 * @param String
	 *            fileName Name of the file of objects to serialize from
	 * @return vector containing objects serialized from disk
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract Vector serializeFromDisk(Class className, String fileName)
			throws IOException, ClassNotFoundException;

}

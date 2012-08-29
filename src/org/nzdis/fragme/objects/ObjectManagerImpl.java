package org.nzdis.fragme.objects;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.ObjectManager;
import org.nzdis.fragme.PeerManager;
import org.nzdis.fragme.exceptions.StartUpException;
import org.nzdis.fragme.factory.FragMeFactory;
import org.jgroups.Address;

/**
 * The ObjectManagerImpl handles all distributed Objects in the system. Every
 * distributed Object has to be registered at the ObjectManager. The
 * ObjectManagerImpl implements the Singleton design pattern
 * 
 * @author Mengqiu Wang, Heiko Wolf, Morgan Bruce, Frank Wu
 * @refactored Morgan Bruce 16/7/2008, Frank Wu 8/8/2008 Go BeiJing
 */
public class ObjectManagerImpl implements ObjectManager {

	/**
	 * The one and only instance of the ObjectManager on this peer
	 */
	private static ObjectManager instance;

	/**
	 * The address of the peer this ObjectManager belongs to
	 */
	private Address myAddr;

	/**
	 * Object storage for other peers
	 */
	private Hashtable storageForOtherPeers = new Hashtable();

	/**
	 * Object storage for dropout peers
	 */
	private Hashtable storageForDropOutPeers = new Hashtable();

	/**
	 * Object storage for its own objects
	 */
	private ObjectStorageForPeer ownObjects = new ObjectStorageForPeer();

	/**
	 * A temporary Vector holds all the objects in the system
	 */
	private Vector allObjects = new Vector();

	/**
	 * Default constructor
	 */
	private ObjectManagerImpl() {
	}

	/**
	 * The entry point to get a reference to the ObjectManager; an
	 * implementation of the singleton pattern
	 * 
	 * @return reference to the ObjectManager
	 * @throws StartUpException
	 *             if ObjectManager has not been started properly
	 */
	public static ObjectManager getInstance() throws StartUpException {
		if (instance == null) {
			throw new StartUpException("ObjectManager doesn't exist");
		}
		return instance;
	}

	/**
	 * Called by ControlCenter to start up the ObjectManager
	 * 
	 * @return reference to the ObjectManager
	 * @throws StartUpException
	 *             if the ObjectManager has already been started
	 */
	public static ObjectManager startObjectManager() throws StartUpException {
		if (instance != null) {
			throw new StartUpException("ObjectManager has already been started");
		}
		instance = new ObjectManagerImpl();
		return instance;
	}

	/**
	 * Stops the Object Manager.
	 */
	public static void stopObjectManager() {
		instance = null;
	}

	/*
	 * ------------------------------- Interface ObjectManager
	 * ------------------------------
	 */

	public Vector getOwnObjects() {
		synchronized (allObjects) {
			allObjects.clear();
			synchronized (ownObjects) {
				allObjects.addAll(ownObjects.getObjects());
				// For some incomprehensible reason, this code added all own
				// objects, then all
				// other objects. Personal theory is a class copied
				// getAllObjects into here
				// on accident - Morgan Bruce.

				// Enumeration e = storageForOtherPeers.elements();
				// while (e.hasMoreElements()) {
				// allObjects.addAll(((ObjectStorageForPeer)
				// e.nextElement()).getObjects());
				// } //end while

			} // end synchronized(ownObjects)
			return allObjects;
		} // end synchronized(allObjects)
	}

	/**
	 * Parse in the name of a class and returns a vector of objects that the
	 * peer owns.
	 * 
	 * @param className
	 *            The name of the class
	 * @return A vector of own objects of a peer
	 */
	public Vector getOwnObjects(Class className) {
		synchronized (allObjects) {
			allObjects.clear();
			synchronized (ownObjects) {
				Vector v = ownObjects.getObjects(className);
				allObjects.addAll(v);
			} // end synchronized(ownObjects)
			return allObjects;
		} // end synchronized(allObjects)
	}

	/**
	 * Returns a vector of all the objects in the system.
	 * 
	 * @return a vector of all the objects
	 */
	public Vector getAllObjects() {
		synchronized (allObjects) {
			allObjects.clear();
			synchronized (storageForOtherPeers) {
				synchronized (ownObjects) {
					allObjects.addAll(ownObjects.getObjects());
					Enumeration e = storageForOtherPeers.elements();
					while (e.hasMoreElements()) {
						allObjects.addAll(((ObjectStorageForPeer) e
								.nextElement()).getObjects());
					} // end while
				} // end synchronized(ownObjects)
			} // end synchronized(storageForOtherPeers)
			return allObjects;
		} // end synchronized(allObjects)
	}

	/**
	 * Returns vector of all the objects that is an instance of a specified
	 * class parsed in
	 * 
	 * @param className
	 *            the name of a class
	 * @return A vector of all the objects that is an instance of a class
	 */
	public Vector getAllObjects(Class className) {
		synchronized (allObjects) {
			allObjects.clear();
			synchronized (storageForOtherPeers) {
				synchronized (ownObjects) {
					Vector v = ownObjects.getObjects(className);
					allObjects.addAll(v);
					Enumeration e = storageForOtherPeers.elements();
					while (e.hasMoreElements()) {
						Vector otherPeerV = ((ObjectStorageForPeer) e
								.nextElement()).getObjects(className);
						allObjects.addAll(otherPeerV);
					} // end while
				} // end synchronized(ownObjects)
			} // end synchronized(storageForOtherPeers)
			return allObjects;
		} // end synchronized(allObjects)
	}

	/**
	 * Sets the address of a peer
	 * 
	 * @param myAddr
	 *            the Address of a peer
	 */
	public void setMyAddress(Address myAddr) {
		this.myAddr = myAddr;
	}

	/**
	 * Returns the address of a peer
	 * 
	 * @return the address of a peer
	 */
	public Address getMyAddress() {
		return myAddr;
	}

	/**
	 * Saves an object to the file objectsOnDisk.txt
	 * 
	 * @param object
	 *            the object to be saved to disk
	 */
	public void serializeToDisk(Object object) throws IOException {
		serializeToDisk(object, "objectsOnDisk.txt");
	}

	/**
	 * Saves an object to a file
	 * 
	 * @param object
	 *            the object to be saved
	 * @param fileName
	 *            the name of the file
	 * 
	 * @throws IOException
	 */
	public void serializeToDisk(Object object, String fileName)
			throws IOException {
		try {
			SerializationHelper.serializeToFile(fileName, object);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a vector of objects of a class retrieved from objectsOnDisk.txt
	 * 
	 * @return a vector of objects of a class retrieved from objectsOnDisk.txt
	 */
	public Vector serializeFromDisk(Class className) throws IOException,
			ClassNotFoundException {
		return serializeFromDisk(className, "objectsOnDisk.txt");
	}

	/**
	 * Returns a vector of objects of a class from a file.
	 * 
	 * @return a vector of objects of a class from a file
	 */
	public Vector serializeFromDisk(Class className, String fileName)
			throws IOException, ClassNotFoundException {
		Vector ob = new Vector();
		Vector fn = new Vector();
		try {
			ob = (Vector) SerializationHelper
					.deserializeFile(fileName);
			for (int i = 0; i < ob.size(); i++) {
				if (ob.get(i).getClass().equals(className)) {
					fn.add(ob.get(i));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fn;
	}

	/**
	 * Push changes of an FMeObject to notify other peers
	 * 
	 * @param object
	 *            the FMeObject changed
	 */
	public void pushChange(FMeObject object) {
		PeerManager pm = ControlCenter.getPeerManager();
		if (object.getOwnerAddr().equals(myAddr)) {
			pm.send(ControlCenter.MODIFY, object, null);
		} else {
			pm.send(ControlCenter.MODIFY, object, object.getOwnerAddr());
		}
	}

	/**
	 * Push changes of a field of a FMeObject to notify other peers
	 * 
	 * @param object
	 *            the FMeObject changed
	 * @param fieldName
	 *            the name of the field changed
	 */
	public void pushChange(FMeObject object, String fieldName) {
		String tempFieldName = fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1, fieldName.length());
		FMeObjectReflection reflectable = null;
		try {
			Method m = object.getClass().getMethod("get" + tempFieldName,
					new Class[] {});
			Object o = m.invoke(object, new Object[] {});

			reflectable = new FMeObjectReflection(fieldName, o, object.getId());
		} catch (Exception e) {
			e.printStackTrace();
			// } catch (NoSuchMethodException e) {
			// e.printStackTrace();
			// } catch (IllegalArgumentException e) {
			// e.printStackTrace();
			// } catch (IllegalAccessException e) {
			// e.printStackTrace();
			// } catch (InvocationTargetException e) {
			// e.printStackTrace();
		}
		PeerManager pm = ControlCenter.getPeerManager();

		if (object.getOwnerAddr().equals(myAddr)) {
			pm.send(ControlCenter.MODIFY, reflectable, null);
		} else {
			pm.send(ControlCenter.MODIFY, reflectable, object.getOwnerAddr());
		}
	}

	/**
	 * Adds the object to ownObjects
	 * 
	 * @param object
	 *            object to be added to ownObjects
	 */
	public void addOwnObject(FMeObject object) {
		synchronized (ownObjects) {
			ownObjects.addObject(object);
		}
	}

	/**
	 * This private method is called by receiveChange() to add objects of other
	 * peers
	 * 
	 * @param object
	 *            object to be added
	 */
	private void addObjectOfOtherPeer(FMeObject object) {
		synchronized (storageForOtherPeers) {
			if (storageForOtherPeers.get(object.getOwnerAddr()) == null) {
				System.out.println("not allocated memory for peer yet");
			}
			ObjectStorageForPeer peerStorage = (ObjectStorageForPeer) storageForOtherPeers
					.get(object.getOwnerAddr());
			peerStorage.addObject(object);
		}
	}

	/**
	 * Decides whether the object is own object or other peer's object and call
	 * the appropriate method to add the object
	 * 
	 * @param object
	 *            object to be added
	 */
	public void addObject(FMeObject object) {
		Address ownerAddr = object.getOwnerAddr();
		if (ownerAddr.equals(myAddr)) {
			addOwnObject(object);
		} else {
			addObjectOfOtherPeer(object);
		}
	}

	public FMeObject lookup(FMeObject object) {
		Vector v = getAllObjects(object.getClass());
		synchronized (storageForOtherPeers) {
			synchronized (ownObjects) {
				for (int i = 0; i < v.size(); i++) {
					FMeObject obj = (FMeObject) v.get(i);
					if (obj.equals(object)) {
						return obj;
					}
				}
			}
		}
		return null;
	}

	public FMeObject lookupById(int id) {
		Vector v = getAllObjects();

		synchronized (storageForOtherPeers) {
			synchronized (ownObjects) {
				for (int i = 0; i < v.size(); i++) {
					FMeObject obj = (FMeObject) v.get(i);
					if (obj.getId() == id) {
						return obj;
					}
				}
				return null;
			}
		}
	}

	public void receiveChange(FMeObject object) {
		if (object.getOwnerAddr().equals(myAddr)) {
			ControlCenter.getPeerManager().send(ControlCenter.MODIFY, object,
					null);
		}
	}

	public void allocateSpaceForPeer(Address addr) {
		synchronized (storageForOtherPeers) {
			if (!storageForOtherPeers.containsKey(addr)) {
				storageForOtherPeers.put(addr, new ObjectStorageForPeer());
			}
		}
	}

	public void sendObjectsToNewPeer(Address addr) {
		// send our own objects
		Vector objects = ownObjects.getObjects();
		PeerManager pm = ControlCenter.getPeerManager();
		for (int i = 0; i < objects.size(); i++) {
			pm.send(ControlCenter.MODIFY, objects.get(i), addr);
		}
		// checks if we keep copy of this peer's previous objects
		String peerName = pm.getPeerName(addr);

		if (storageForDropOutPeers.containsKey(peerName)) {
			ObjectStorageForPeer peerStorage = (ObjectStorageForPeer) storageForDropOutPeers
					.get(peerName);
			objects = peerStorage.getObjects();
			int i = 0;
			for (; i < objects.size(); i++) {
				FMeObject object = (FMeObject) objects.get(i);
				object.setOwnerAddr(addr);
				object.setId(i);
				pm.send(ControlCenter.MODIFY, objects.get(i), addr);
			}
			int factoryStartingSeq = i;
			peerStorage.clear();
			storageForDropOutPeers.remove(peerName);
			peerStorage = null;
			pm.send(ControlCenter.SYNCHRONIZE, new Integer(factoryStartingSeq),
					addr);
		}
	}

	public void deletePeer(Address addr) {
		ObjectStorageForPeer peerStorage = (ObjectStorageForPeer) storageForOtherPeers
				.get(addr);
		peerStorage.clear();
		storageForOtherPeers.remove(addr);
		peerStorage = null;
	}

	public void deleteObject(Address addr, int id) {
		if (addr.equals(myAddr)) {
			synchronized (ownObjects) {
				ownObjects.deleteObject(id);
			}
			ControlCenter.getPeerManager().send(ControlCenter.DELETE,
					new Integer(id), null);
		} else {
			synchronized (storageForOtherPeers) {
				ObjectStorageForPeer peerStorage = (ObjectStorageForPeer) storageForOtherPeers
						.get(addr);
				peerStorage.deleteObject(id);
			}
		}
	}

	public void requestDeleteObject(Address addr, int id) {
		ControlCenter.getPeerManager().send(ControlCenter.REQUEST_DELETE,
				new Integer(id), addr);
	}

	public void delegatePeerObjects(Address addr) {
		String peerName = ControlCenter.getPeerManager().getPeerName(addr);
		synchronized (storageForOtherPeers) {
			ObjectStorageForPeer peerStorage = (ObjectStorageForPeer) storageForOtherPeers
					.get(addr);
			synchronized (storageForDropOutPeers) {
				Vector objects = peerStorage.getObjects();
				for (int i = 0; i < objects.size(); i++) {
					FMeObject object = (FMeObject) objects.get(i);
					if (object instanceof Transferable) {
						peerStorage.deleteObjectInDropOutCase(object.getId());
						object.setId(FragMeFactory.generateObjectId());
						object.setOwnerAddr(myAddr);
						addOwnObject(object);
						pushChange(object);
					}
				}
				if (storageForDropOutPeers.containsKey(peerName)) {
					System.out
							.println("storageForDropOutPeers contains old data");
				} else if (peerStorage.getObjects().size() != 0) {
					storageForDropOutPeers.put(peerName, peerStorage);
				}
				storageForOtherPeers.remove(addr);
			}// end synchronized(storageForDropOutPeers)
		}// end synchronized (storageForOtherPeers)
	}

	/*
	 * ------------------------------- End Interface ObjectManager
	 * -----------------------------
	 */

}

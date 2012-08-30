package org.nzdis.fragme.factory;

import java.util.Hashtable;
import java.util.Vector;
import org.nzdis.fragme.ObjectManager;
import org.nzdis.fragme.objects.FMeObject;
import org.nzdis.fragme.ControlCenter;

/**
 * FragMeFactory.java<br>
 * Created: 15/08/2003 11:40:24<br>
 * Modified: 20/08/2003 10:54:40 (MEZ +1h)<br>
 * 
 * @author <a href="mailto:benjamin.herrmann@in.tum.de">Benjamin Herrmann</a>
 * @version $Revision: 1.1 $Date: 2008/09/03 04:24:50 $
 */
public abstract class FragMeFactory {
	// all member are kept static as they are only needed once.

	/**
	 * for each key (FMEObjectName as string) this Hashtable stores all free
	 * FMEObjects of this type in a ArrayList
	 */
	private static Hashtable freeObjects = new Hashtable();

	public static int objectIdCounter = 0;

	private static Hashtable factories4Objects = new Hashtable();

	/**
	 * this is the maximum number of free objects that are kept of each type
	 * BEFORE freeing the objects
	 */
	private static final int maxNumOfFreeObjects = 5;

	// /**
	// * a increasing number used as a unique identifier for each generated
	// object
	// * in this FactoryClass
	// */
	// private static int id = -1;

	/**
	 * create of a FMeObjectFactory Class returns a FMeObject, a
	 * FMeSerializedFactory Class returns a FMeSerialized...
	 * 
	 * @return
	 */
	protected abstract FactoryObject create();

	/**
	 * Returns the Factories4Objects Hashtable.
	 * 
	 * @return Factories4Objecgts
	 */
	public static Hashtable getFactories4Objects() {
		return factories4Objects;
	}

	/**
	 * Returns an instance of an object of type 'type'. This can be either a
	 * newly created object or one of the reusable objects stored in the
	 * freeObjects ArrayList. The String type is the return value of the
	 * getName() method of the specific FMeObject
	 * 
	 * @param type
	 *            the type of object to get
	 * @return an instance of a factory object of the type parsed in
	 */
	public static final FactoryObject getObject(Class type) throws Exception {
		if (freeObjects.get(type) == null
				|| ((Vector) freeObjects.get(type)).size() == 0) {
			// no old Factorizable objects available => generate a new one...
			try {
				FactoryObject obj = (FactoryObject) ((FragMeFactory) factories4Objects
						.get(type)).create();
				return obj;
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Factory class for FMeObject type '" + type
						+ "' was not found!");
			}
		} else {

			// an old instance of a FMeObject of the specific type was found.
			// This is returned, it cannot be ensured
			// however what initial values it has...

			// no new instances of classes are created, we merely save
			// references to existing instances
			Vector list = ((Vector) freeObjects.get(type));
			FactoryObject obj = (FactoryObject) list.elementAt(list.size() - 1);
			list.removeElementAt(list.size() - 1); // should be done in O(1)
			return obj;
		}
	}

	/**
	 * This method creates and returns an instance of a new FMeObject (the exact
	 * type specified by 'type') Let this new object be know as obj, then obj is
	 * owned by this Peer and therefore registered with the ObjectManagerImpl as
	 * an owned object!
	 * 
	 * @param type
	 *            the type of object to be created
	 * @return an instance of a new FMeObject of type parsed in
	 */
	public static final FMeObject createNewObject(Class type) throws Exception {
		FMeObject object = (FMeObject) FragMeFactory.getObject(type);
		ObjectManager OM = ControlCenter.getObjectManager();

		// set the ids of the new object. Remember that this Peer owns the
		// object so OwnerID
		// is set to FMeObjectId.LOCAL_OBJECT!
		// this has to be done BEFORE calling obMan.addOwnedObject!
		object.setOwnerAddr(OM.getMyAddress());
		object.setId(generateObjectId());
		OM.addOwnObject(object);
		OM.pushChange(object);

		return object;
	}

	/**
	 * Saves 'obj' as a reusable object of type 'type'. Attention: Make sure
	 * that in the main program the reference to obj is set to null after
	 * calling this method!
	 * 
	 * @param obj
	 *            the FactoryObject to be saved
	 * @param type
	 *            the type of the object
	 * 
	 */
	public static final void freeObject(FactoryObject obj, Class type) {
		if (freeObjects.get(type) == null) {
			// ArrayList of free Objects for this type does not already exist =>
			// create it
			Vector list = new Vector(2); // save memory by using 2 as initial
			// size instead of the maximum
			list.addElement(obj);
			freeObjects.put(type, list);
		} else {
			// ArrayList of free Factorizable objects for this type already
			// exists => append obj and check for number
			// of free Factorizable objects of this type
			Vector list = (Vector) freeObjects.get(type);
			if (list.size() < maxNumOfFreeObjects) {
				list.addElement(obj);
			} else {
				obj = null;
			}

		}
	}

	/**
	 * generate the next factory id(guaranteed to be unique,used to identify
	 * objects)
	 * 
	 * @return int the next factory id
	 */
	public static final int generateObjectId() {
		objectIdCounter++;
		return objectIdCounter;
	}

	/**
	 * This method adds an implementation of the abstract FragMeFactory class to
	 * its static Hashtable. The key (under which the new Factory is saved) is a
	 * String describing the object type (as usually returned by the static
	 * getName() method of each Factorizable object). Each Factory class
	 * implementation can instantiate one object type by defining a body for the
	 * abstract create() method.
	 * 
	 * @param factory
	 *            the factory to be added to factories4Objects Hashtable
	 * @param type
	 *            the type of the factory
	 * 
	 */
	public static final void addFactory(FragMeFactory factory, Class type) {
		factories4Objects.put(type, factory);
	}

	/**
	 * This method returns to a given FMeSerialised the "real" object.
	 * 
	 * @param serObject
	 *            the serialized FMeObject
	 * @return the deserialized version of the object, or null if an exception
	 *         occurred
	 */
	public static final FMeObject deserialize(FMeObject serObject) {
		try {
			// create FMeObject
			FMeObject object = (FMeObject) getObject(serObject.getClass());

			object.setId(serObject.getId());
			object.setOwnerAddr(serObject.getOwnerAddr());
			ObjectManager OM = ControlCenter.getObjectManager();

			// look up to see if this object already exists in the game
			FMeObject existObject = OM.lookup(object);

			if (existObject == null) {
				System.out.println("Creating a new object!");
				object.deserialize(serObject);
				serObject = null;
				OM.addObject(object);
				return object;
			} else {
				System.out.println("Reusing an existing object!");
				existObject.deserialize(serObject);
				serObject = null;
				freeObject(object, object.getClass());
				return existObject;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the free objects hashtable
	 * 
	 * @return the free objects hashtable
	 */
	public static Hashtable getFreeObjects() {
		return freeObjects;
	}

	/**
	 * Sets freeObjects to a new hashtable, used for testing only
	 */
	public static void resetFreeObjects() {
		freeObjects = new Hashtable();
	}

	/**
	 * Returns the maximum number of free objects, used for testing only
	 * 
	 * @return the maxinum number of free objects
	 */
	public static int getMaxNumOfFreeObjects() {
		return maxNumOfFreeObjects;
	}
}

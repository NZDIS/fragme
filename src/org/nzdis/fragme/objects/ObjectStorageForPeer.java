package org.nzdis.fragme.objects;

import org.nzdis.fragme.factory.FragMeFactory;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class provides storage of objects for peers
 * 
 * @author Heiko Wolf, Mengqiu Wang
 * @refactored Morgan Bruce, 16/7/2008 
 */
public class ObjectStorageForPeer {
 
	/**
	 * FMeObjects are stored within this hash table, which 
	 * consists of vectors.  Each vector is keyed by the 
	 * class name of its members.
	 */
	private Hashtable table = new Hashtable();

	public ObjectStorageForPeer() {}

  	/**
  	 * Returns all FMeObjects stored in this storage
  	 *
  	 * @return vector containing these objects
  	 */
  	public Vector getObjects() {
  		Vector v = new Vector();
  		synchronized (table) {
  			Enumeration e = table.elements();
  			while (e.hasMoreElements())
  				v.addAll( (Vector) e.nextElement());
  		}
  		return v;
  	}

  	/**
  	 * Return FMeObjects in this storage of a particular class type (or its parent class type)
  	 *
  	 * @return Vector
  	 */
  	public Vector getObjects(Class className) {
  		Vector v = new Vector();
  		synchronized (table) {
  			Enumeration keys = table.keys();
  			while (keys.hasMoreElements()) {
  				Class key = (Class) keys.nextElement();
  				if(key.equals(className)){
  					v.addAll( (Vector) table.get(key));
  					continue;
  				}
  				Class parent = key.getSuperclass();
  				while (!parent.equals(FMeObject.class) && !parent.equals(Object.class) ) {
  					if (parent.equals(className)){
  						v.addAll( (Vector) table.get(key));
  						break;
  					}
  					else
  						parent = parent.getSuperclass();
  				}//end while(!parent...)
  			}//end while(keys.hasMoreElements())
  			return v;
  		}
  	}

  	/**
  	 * Deletes an FMeObject from the hash table if a peer has dropped out
  	 * 
  	 * @param id id of object to be deleted
  	 */
  	public void deleteObjectInDropOutCase(String id){
  		Enumeration elements = table.elements();
  		while(elements.hasMoreElements()){
  			Vector aV = (Vector)elements.nextElement();
  			Iterator it = aV.iterator();
  			while(it.hasNext()){
  				FMeObject object = (FMeObject)it.next();
  				if(object.getId().equals(id)){
  					it.remove();
  					object = null;
  					return;
  				}
  			}//end while(it.hasNext())
  		}//end while(elements...
  	}

  	/**
  	 * Deletes FMeObject with id from storage
  	 * 
  	 * @param id id of object to delete
  	 */
  	public void deleteObject(String id) {
  		Enumeration elements = table.elements();
  		while(elements.hasMoreElements()){
  			Vector aV = (Vector)elements.nextElement();
  			Iterator it = aV.iterator();
  			while(it.hasNext()){
  				FMeObject object = (FMeObject)it.next();
  				if(object.getId().equals(id)){
  					object.informChangeObserversDeleted();
  					FragMeFactory.freeObject(object, object.getClass());
  					it.remove();
  					object = null;
  					return;
  				}
  			}//end while(it.hasNext())
  		}//end while(elements...
  	}

  	/**
  	 * Clears the object storage
  	 *
  	 */
  	public void clear(){
  		Enumeration elements = table.elements();
  		while(elements.hasMoreElements()){
  			Vector aV = (Vector)elements.nextElement();
  			Iterator it = aV.iterator();
  			while(it.hasNext()){
  				FMeObject object = (FMeObject)it.next();
  				FragMeFactory.freeObject(object, object.getClass());
  				it.remove();
  			}//end while(it.hasNext())
  		}//end while(elements...
  		elements = null;
  	}

  	/**
  	 * Adds an FMeObject to the storage; also updates any existing
  	 * version of this object
  	 *
  	 * @param object FMeObject to add to storage
  	 */
  	public void addObject(FMeObject object) {
  		Class className = object.getClass();
  		//if the record for this type of object exists
  		synchronized (table) {
  			if (table.containsKey(className)) {
  				Vector aV = (Vector) table.get(className);
  				//Remember,we implemented equals in FMeObject
  				//if an object that equals to this new object already exist
  				//we should delete the old object
  				if (aV.contains(object)) {
  					System.out.println("CONFLICT, add an already existing object : "+object);
  				}
  				aV.add(object);
  			} else {
  				Vector aV = new Vector();
  				aV.add(object);
  				table.put(className, aV);
  			} //end if(table.containsKey(...))
  		} //end synchronized(table)
  	}
}

package org.nzdis.fragme.objects;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
 
/**
 * SerializationHelper provides methods for the serialization 
 * of objects to and from files
 * 
 * @refactored Morgan Bruce 16/7/2008
 */
public class SerializationHelper {
	
    /**
     * Deserializes an object from a file 
     * 
     * @param fileName name of the file to be deserialized from
     * @return the deserialized Object
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static Object deserializeFile(String fileName) 
    	throws IOException, ClassNotFoundException
    {
    	FileInputStream ips = new FileInputStream(fileName);
    	ObjectInputStream ip = new ObjectInputStream(ips);	
    	Object obj = ip.readObject();
    	ips.close();
    	return obj;
    }
 
    /**
     * Serializes an object to a file
     * 
     * @param fileName the name of a file where an object would be serialized.
     * @param object that need to be serialized
     * @throws IOException 
     */
    public static void serializeToFile(String fileName, Object object) throws IOException {
    	FileOutputStream ops = new FileOutputStream(fileName);
        ObjectOutputStream ip= new ObjectOutputStream(ops);
        ip.writeObject(object);
        
        ip.flush();
    	ops.close();
    }
}

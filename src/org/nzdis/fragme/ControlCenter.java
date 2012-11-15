package org.nzdis.fragme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observer;
import java.util.Vector;
import org.nzdis.fragme.exceptions.StartUpException;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.helpers.StartupWaitHelper;
import org.nzdis.fragme.objects.Message;
import org.nzdis.fragme.objects.NewFMeObjectObserver;
import org.nzdis.fragme.objects.ObjectManagerImpl;
import org.nzdis.fragme.peers.PeerManagerImpl;
import org.nzdis.fragme.peers.TypeWrappers.FlagBool;
import org.jgroups.Address;
import org.jgroups.Global;

/**
 * The ControlCenter provides the single point of control for the FragMe
 * framework: a FragMe application should begin by using the ControlCenter to
 * setup its connection to the network.
 * 
 * The ControlCenter also interacts with the PeerManager and ObjectManager,
 * which also communicate through this class. Definitions of message types used
 * between PeerManagers are also defined within the ControlCenter
 * 
 * @author Mengqiu Wang, Heiko Wolf, Morgan Bruce, Frank Wu
 * @refactored Morgan Bruce, Frank Wu 14/7/2008 - 17/7/2008
 * @refactored Nathan D. Lewis, Christopher Frantz - September 2012
 */
public abstract class ControlCenter {

	/** Print debug information about JGroup channel establishment */
	public static final boolean DEBUG_CHANNEL_SETUP = false;
	/** Print debug about synchronization of peers with group */
	public static final boolean DEBUG_SYNCHRONIZATION = false;
	/** Print debug about send operations (send, receive, serialization) */
	public static final boolean DEBUG_SENDING = false;
	/** Print debug about object creation and reuse in FragMeFactory */
	public static final boolean DEBUG_OBJECT_MANAGEMENT = false;
	
	/**
	 * Static instance of this peer's PeerManager
	 */
	private static PeerManager PM;

	/**
	 * Static instance of this peer's ObjectManager
	 */
	private static ObjectManager OM;

	/**
	 * Vector of objects to be serialised
	 */
	private static Vector seriList = new Vector();

	private static Message tempMessage = null;

	/**
	 * This message content is used to notify peers of the completion of sending
	 * an object to a new peer
	 */
	public static final String OBJECTS_HAVE_BEEN_SENT = "OBJECTS_HAVE_BEEN_SENT";

	/**
	 * This message content is used to notify peers of sending peername.
	 */
	public static final String REQUEST_PEER_NAME = "REQUEST_PEER_NAME";

	/**
	 * This message content is used to notify peers of the completion of a space
	 * allocation event
	 */
	public static final String SPACE_HAS_BEEN_ALLOCATED = "SPACE_HAS_BEEN_ALLOCATED";

	/**
	 * The following fields denote different types of messages that can be
	 * sent between peers
	 */
	//public static final String SYNCHRONIZE = "SYNCHRONIZE";
	public static final String MODIFY = "MODIFY";
	public static final String MODIFY_FAILED = "MODIFY_FAILED";
	public static final String REQUEST_DELETE = "REQUEST_DELETE";
	public static final String DELETE = "DELETE";
	public static final String REQUEST_DELETE_FAILED = "REQUEST_DELETE_FAILED";
	public static final String REQUEST_DELEGATE_OWNERSHIP = "REQUEST_DELEGATE_OWNERSHIP";
	public static final String DELEGATED_OWNERSHIP = "DELEGATED_OWNERSHIP";
	public static final String REQUEST_DELEGATE_OWNERSHIP_FAILED = "REQUEST_DELEGATE_OWNERSHIP_FAILED";
	public static final String NOTIFY = "NOTIFY";

	/**
	 * Static flag used for synchronization with the PeerManager at startup time
	 */
	public static final FlagBool allPeersHaveFinishedSetup = new FlagBool(false);

	/**
	 * Gets ObjectManager of this ControlCenter
	 * 
	 * @return ObjectManager
	 */
	public static ObjectManager getObjectManager() {
		return OM;
	}

	/**
	 * Gets PeerManager of this ControlCenter
	 * 
	 * @return PeerManager
	 */
	public static PeerManager getPeerManager() {
		return PM;
	}

	/**
	 * Gets all current objects in the application
	 * 
	 * @return a Vector containing all current objects
	 */
	public static Vector getAllObjects() {
		return OM.getAllObjects();
	}

	/**
	 * Get all current objects in the application of a particular class type
	 * 
	 * @param className
	 *            name of class to find objects of
	 * 
	 * @return a Vector containing all current objects of a class type
	 */
	public static Vector getAllObjects(Class className) {
		return OM.getAllObjects(className);
	}

	/**
	 * Get all own objects in the application of a particular class type
	 * 
	 * @param className
	 *            name of class to find objects of
	 * 
	 * @return a Vector containing owned current objects of a class type
	 */
	public static Vector getOwnObjects(Class className) {
		return OM.getOwnObjects(className);
	}

	/**
	 * Get all own objects in the application, regardless of type
	 * 
	 * @return a Vector containing all current owned objects
	 */
	public static Vector getOwnObjects() {
		return OM.getOwnObjects();
	}

	public static boolean setUpConnectionsWithHelper(String groupName, StartupWaitHelper helper) {
		boolean startupResult = setUpConnections(groupName);
		helper.waitForCondition();
		return startupResult;
	}
	
	public static boolean setUpConnectionsWithHelper(String groupName, String peerName, StartupWaitHelper helper) {
		return setUpConnectionsWithHelper(groupName, peerName, null, helper);
	}
	
	public static boolean setUpConnectionsWithHelper(String groupName, String peerName, String bindAddress, StartupWaitHelper helper) {
		boolean startupResult = setUpConnections(groupName, peerName, bindAddress);
		helper.waitForCondition();
		return startupResult;
	}
	
	/**
	 * Sets up the peer connection and takes care of all the initialization
	 * processes. This method takes a group name only, while the user will be
	 * prompted on the command line for a peer name. This method, or
	 * setUpConnections(String, String) should be called by the application to
	 * initialise the ControlCenter
	 * 
	 * @param groupName
	 *            group name to initialise
	 * 
	 * @return true if the peer has existed before, return false otherwise
	 */
	public static boolean setUpConnections(String groupName) {
		System.out.println("Please enter your peer name");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String peerName = null;
		while (true) {
			try {
				peerName = br.readLine().trim();
				if (peerName == null || peerName.equals("")) {
					System.out.println("invalid peer name");
				} else {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return setUpConnections(groupName, peerName, null);
	}
	
	public static boolean setUpConnections(String groupName, String peerName) {
		return setUpConnections(groupName, peerName, null);
	}

	/**
	 * Sets up the peer connection and takes care of all the initialization
	 * processes. This method takes a group name and a peer name. This method,
	 * or setUpConnections(String) should be called by the application to
	 * initialise the ControlCenter.
	 * 
	 * @param groupName
	 *            group name to initialise
	 * @param peerName
	 *            peer name to initialise
	 * @param bindAddress
	 * 		      address to bind to
	 * 
	 * @return true if the peer has existed before, return false otherwise
	 */
	public static boolean setUpConnections(String groupName, String peerName, String bindAddress) {
		if ((bindAddress != null) && (bindAddress.length() > 0)) {
			System.setProperty(Global.BIND_ADDR, bindAddress);
		}
		System.setProperty(Global.USE_JDK_LOGGER, "true");
		try {
			OM = ObjectManagerImpl.startObjectManager();
			PM = PeerManagerImpl.startPeerManager(groupName, peerName);
			boolean droppedOutBefore = PM.activate();
			checkPeersConnected();
			return droppedOutBefore;
		} catch (StartUpException e) {
			System.out.println(e);
			return true;
		}
	}
	
	/**
	 * Register a NewFMeObjectObserver
	 * @param observer
	 */
	public static void registerNewFMeObjectObserver(NewFMeObjectObserver observer) {
		PeerManagerImpl pmi = (PeerManagerImpl)PM;
		pmi.register(observer);
	}

	/**
	 * Creates new objects of Class type. Must be called by the application when
	 * creating new objects, as all shared objects must be managed by the
	 * ControlCenter
	 * 
	 * @param type
	 *            the Class of Object to create (must be subclass of
	 *            FactoryObject)
	 * @return the created object
	 */
	public static Object createNewObject(Class type) {
		try {
			return FragMeFactory.createNewObject(type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the address of this peer
	 * 
	 * @return the peer's own address
	 */
	public static Address getMyAddress() {
		return PM.getMyAddress();
	}

	/**
	 * Returns the name of this peer
	 * 
	 * @return the peer's own name
	 */
	public static String getMyName() {
		return PM.getMyPeerName();
	}

	/**
	 * Returns the peer address for a given name
	 * 
	 * @param name
	 *            the name we want to know address of
	 * @return the peer address for the given name
	 */
	public static Address getPeerAddress(String name) {
		return PM.getPeerAddress(name);
	}

	/**
	 * Returns the peer name for a given address
	 * 
	 * @param addr
	 *            the address we want to know name of
	 * @return the peer name for the given address
	 */
	public static String getPeerName(Address addr) {
		return PM.getPeerName(addr);
	}

	/**
	 * Returns the group this ControlCenter/application/peer belongs to
	 * 
	 * @return the name of this group
	 */
	public static String getGroupName() {
		return PM.getGroupName();
	}

	/**
	 * Returns the total number of peers in the application
	 * 
	 * @return the total number of peers
	 */
	public static int getNoOfPeers() {
		return PeerManagerImpl.getNoOfPeers().getValue();
	}

	/**
	 * Private method called by setUpConnections for synchronization with
	 * PeerManager
	 */
	private static void checkPeersConnected() {
		if (getNoOfPeers() > 0) {
			synchronized (allPeersHaveFinishedSetup) {
				while (!allPeersHaveFinishedSetup.getValue()) {
					try {
						allPeersHaveFinishedSetup.wait();
					} catch (InterruptedException ex) {
						System.out.println("Interrupted");
					}
				} // end while
			} // end synchronized
		}
	}
	
	/**
	 * Closes connections by stopping peer and object managers
	 * 
	 */
	public static void closeUpConnections() {
		PeerManagerImpl.stopPeerManager();
		ObjectManagerImpl.stopObjectManager();
		OM = null;
		PM = null;
		System.out.println("Connections closed.");
	}

	/* ------------------------------- Messaging ------------------------------- */

	/**
	 * Returns the message object - a new message object is created if it is the
	 * first peer, or the existing object is retrieved.
	 * 
	 * @return the message object
	 */
	public static Message getMessageObject() {
		Vector MessageObject = ControlCenter.getAllObjects(Message.class);
		if (MessageObject.size() == 0) {
			// then this peer is the first to start so it should create the
			// Manager object
			tempMessage = (Message) ControlCenter
					.createNewObject(Message.class);
		} else {
			// this is not the first peer, so we should get the manager from the
			// framework
			tempMessage = (Message) MessageObject.get(0);
		}
		return tempMessage;
	}

	/**
	 * Called by a peer to allow it to receive messages
	 * 
	 * @param caller
	 *            The peer that calls this method to receive messages
	 */
	public static void receiveMessages(Object caller) {
		MessageManager messMan = new MessageManager(caller);
		Message message = getMessageObject();
		message.register(messMan);
	}

	/**
	 * Sends a message with content only
	 * 
	 * @param content
	 *            Content of the message
	 */
	public static void sendMessage(String content) {
		Message message = (Message) getMessageObject();
		message.setAll(PM.getMyPeerName(), null, content, null);
		message.change();
	}

	/**
	 * Sends a message with recipients specified.
	 * 
	 * @param recipients
	 *            Recipients of the message
	 * @param content
	 *            Content of the message
	 */
	public static void sendMessage(Vector recipients, String content) {
		Message message = (Message) getMessageObject();
		message.setAll(PM.getMyPeerName(), recipients, content, null);
		message.change();
	}

	/**
	 * Sends a message with message type - for example, private message,
	 * broadcast message type.
	 * 
	 * @param content
	 *            Content of the message
	 * @param type
	 *            Type of the message
	 */
	public static void sendMessage(String content, String type) {
		Message message = (Message) getMessageObject();
		message.setAll(PM.getMyPeerName(), null, content, type);
		message.change();
	}

	/**
	 * Sends a message with recipients and type
	 * 
	 * @param recipients
	 *            Recipients of the message
	 * @param content
	 *            Content of the message
	 * @param type
	 *            Type of the message
	 */
	public static void sendMessage(Vector recipients, String content,
			String type) {
		Message message = (Message) getMessageObject();
		message.setAll(PM.getMyPeerName(), recipients, content, type);
		message.change();
	}

	/*
	 * ------------------------------- end Messaging
	 * -------------------------------
	 */

	/*
	 * ------------------------------- Serialization
	 * -------------------------------
	 */

	/**
	 * Public method called by client applications to serialize objects to disk -
	 * serializes all objects within seriList
	 * 
	 * @throws IOException
	 */
	public static void serializedToFile() throws IOException {
		ControlCenter.getObjectManager().serializeToDisk(seriList);
	}

	/**
	 * Public method called by client applications to serialize objects to disk -
	 * serializes all objects within seriList
	 * 
	 * @throws IOException
	 */
	public static void serializedToFile(String fileName) throws IOException {
		ControlCenter.getObjectManager().serializeToDisk(seriList, fileName);
	}

	/**
	 * Public method called by client applications for serialize objects from
	 * disk
	 * 
	 * @param className
	 *            Class of which to serialize from disk
	 * @return a Vector containing objects of className
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Vector serializedFromFile(Class className)
			throws IOException, ClassNotFoundException {
		return ControlCenter.getObjectManager().serializeFromDisk(className);
	}

	/**
	 * Public method called by client applications for serialize objects from
	 * disk
	 * 
	 * @param className
	 *            Class of which to serialize from disk
	 * @param fileName
	 *            Name of the file to serialize objects from
	 * 
	 * @return a Vector containing objects of className
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Vector serializedFromFile(Class className, String fileName)
			throws IOException, ClassNotFoundException {
		return ControlCenter.getObjectManager().serializeFromDisk(className,
				fileName);
	}

	/**
	 * Adds an object to the seriList
	 * 
	 * @param o
	 *            Object to add
	 */
	public static void addToList(Object o) {
		seriList.add(o);
	}

	/**
	 * Removes an object from the seriList
	 * 
	 * @param o
	 *            Object to remove
	 */
	public static void removeFromList(Object o) {
		seriList.remove(o);
	}

	/**
	 * Clears seriList
	 */
	public static void removeAllFromList() {
		seriList.removeAllElements();
	}

	/**
	 * Returns seriList contents
	 * 
	 * @return a Vector containing objects in seriList
	 */
	public static Vector getSeriList() {
		return seriList;
	}

	/*
	 * ------------------------------- end Serialization
	 * -------------------------------
	 */

}

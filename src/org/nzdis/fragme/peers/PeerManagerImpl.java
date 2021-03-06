package org.nzdis.fragme.peers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.PeerManager;
import org.nzdis.fragme.exceptions.StartUpException;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;
import org.nzdis.fragme.objects.FMeObjectReflection;
import org.nzdis.fragme.objects.FMeObservable;
import org.nzdis.fragme.peers.TypeWrappers.FlagBool;
import org.nzdis.fragme.peers.TypeWrappers.FlagInt;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

/**
 * Implementation of the PeerManager Interface
 * 
 * @author Mengqiu Wang, Heiko Wolf
 * @refactored Morgan Bruce, Frank Wu 1/8/2008
 * @refactored Nathan D. Lewis, Christopher Frantz - September 2012
 */
// Removed "extends Observable"
public class PeerManagerImpl extends FMeObservable implements PeerManager,
		Receiver, ChannelListener {
	
	public static final boolean DEBUG_CHANNEL_SETUP = ControlCenter.DEBUG_CHANNEL_SETUP;
	public static final boolean DEBUG_SYNCHRONIZATION = ControlCenter.DEBUG_SYNCHRONIZATION;
	public static final boolean DEBUG_SENDING = ControlCenter.DEBUG_SENDING;

	/** the singleton instance of PeerManagerImpl */
	private static PeerManagerImpl instance;
	public static final String FACTORY_ID = "FACTORY_ID";
	public static final String PEER_NAME = "PEER_NAME";
	public static final String REPLY_TO_FOSTER = "REPLY_TO_FOSTER";
	public static final String REPLY_TO_BE_FOSTERED = "REPLY_TO_BE_FOSTERED";
	public static final String REQUEST_TO_FOSTER = "REQUEST_TO_FOSTER";
	public static final String REQUEST_TO_BE_FOSTERED = "REQUEST_TO_BE_FOSTERED";
	
	public static final String SHUTDOWN = "SHUTDOWN";

	/** Member variables */
	private boolean receivedMyOwnObjects = false;
	private FlagBool peerListReceived = new FlagBool(false);
	private static FlagInt noOfPeers = new FlagInt(0);

	/** private Address child */
	private Address peerImFostering;
	private Address peerMyChildIsFostering;

	private Random randomNumGenerator = new Random();

	/**
	 * 0 - Not fostered and not fostering. 1 - Fostered but not fostering - this
	 * case is impossible, as we consider fostering a dead child still to be
	 * fostering. 2 - Bot fostered but fostering 3 - Fostered and fostering
	 */
	private FlagBool fosterResult = new FlagBool(false);

	/** The name of the group, parsed by application */
	private String groupName;
	/** The name of the peer */
	private String peerName;
	private ArrayList<Address> members = new ArrayList<Address>();
	/** The address of the current peer */
	//private Address myAddr;
	private Hashtable<Address, String> peerAddressToNameTable = new Hashtable<Address, String>();
	private Hashtable<String, Address> peerNameToAddressTable = new Hashtable<String, Address>();
	// TODO make sure that these are cleaned out when a peer leaves
	private Hashtable<Address, FlagBool> spaceForMeHasBeenAllocated = new Hashtable<Address, FlagBool>();
	private Hashtable<Address, FlagBool> peersWhichHaveSentObjects = new Hashtable<Address, FlagBool>();
	private Hashtable<Address, JoinThread> joinThreads = new Hashtable<Address, JoinThread>(); 
	private MessageContainer mc = new MessageContainer();
	
	private static boolean isRunning = false;
	private SynchronousQueue<Message> sendQueue = new SynchronousQueue<Message>();

	/** The transport channel used */
	private JChannel channel;

	/** indicates if channel connected (as reported by JGroups) */
	private boolean connected = false;
	/** time out for channel activation (in ms) */
	private final int activationTimeOut = 5000;
	/** check iterations for timeout (in ms) */
	private final long timeBetweenActivationChecks = 200;
	/** additional conservative wait to ensure connection is really established (in ms) */
	private final long additionalWaitAfterConnect = 0;
	
	/** protocol stack */
	/*private static String props = "UDP(mcast_addr=224.0.0.0;mcast_port=7500;ip_ttl=32;"
			+ "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):"
			+ "PING(timeout=3000;num_initial_members=3):"
			+ "MERGEFAST:"
			+ "FD_SOCK:"
			+ "VERIFY_SUSPECT(timeout=1000):"
			+ "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):"
			+ "UNICAST(timeout=5000):"
			+ "pbcast.STABLE(desired_avg_gossip=20000):"
			+ "FRAG(frag_size=8096;down_thread=false;up_thread=false):"
			+ "CAUSAL:"
			+ "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;"
			+ "shun=false;print_local_addr=true)";*/
	private static String props = "UDP(mcast_addr=224.0.0.0;mcast_port=7500;ip_ttl=32;"
			+ "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):"
			+ "PING(timeout=3000;num_initial_members=3):"
			+ "FD_SOCK:"
			+ "VERIFY_SUSPECT(timeout=1000):"
			+ "pbcast.NAKACK(retransmit_timeout=100,200,400):"
			+ "pbcast.STABLE(desired_avg_gossip=20000):"
			+ "FRAG(frag_size=8096):"
			+ "pbcast.GMS(join_timeout=5000;print_local_addr=true)";
	
	public Address getMyAddress() {
		return channel.getAddress();
	}

	/**
	 * Constructor sets the groupName and peerName
	 * 
	 * @param groupName
	 *            the name of the group
	 * @param peerName
	 *            the name of the peer
	 */
	private PeerManagerImpl(String groupName, String peerName) {
		this.groupName = groupName;
		this.peerName = peerName;
	}

	/**
	 * Called by the ControlCenter,returns the no. of existing peers, wrapped in
	 * a FlagInt object
	 * 
	 * @return the number of existing peers
	 */
	public static FlagInt getNoOfPeers() {
		return noOfPeers;
	}

	/**
	 * Member Class CheckThread, an instance of this thread should be running at
	 * initialization time
	 */
	class JoinThread extends Thread {
		Address addr;
		boolean isRunning = true;

		/**
		 * Constructor that parses in an address, and starts up the thread
		 * 
		 * @param addr
		 *            the address of a peer
		 */
		public JoinThread(Address addr) {
			this.addr = addr;
			this.setName("JoinThread for " + addr);
			this.start();
		}

		/**
		 * Starts up the thread by sending the peerName to other peers and
		 * allocate space for the peer
		 */
		public void run() {
			// Send peer name early to prevent the situation where the application wants the name for 
			// an unnamed peer (from peerAddressToNameTable). This cannot happen at startup - it only
			// might happen when a new peer has joined, after it has had space allocated and before it's
			// name has been resolved.
			sendFragMessage(peerName, PEER_NAME, addr);
			if(DEBUG_SYNCHRONIZATION){
				System.out.println("Sent PEER_NAME to " + addr);
			}

			// allocate space for the new peer
			ControlCenter.getObjectManager().allocateSpaceForPeer(addr);
			send(ControlCenter.NOTIFY, ControlCenter.SPACE_HAS_BEEN_ALLOCATED, addr);
			if(DEBUG_SYNCHRONIZATION){
				System.out.println("Sent SPACE_HAS_BEEN_ALLOCATED to " + addr);
			}
			
			// wait until we are told that the new peer has allocated space for us
			synchronized (spaceForMeHasBeenAllocated) {
				while ((spaceForMeHasBeenAllocated.get(addr) == null) && isRunning) {
					try {
						spaceForMeHasBeenAllocated.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				spaceForMeHasBeenAllocated.remove(addr);
			} // end of synchronized block
			
			// make sure we get the peer's name before we send objects to him
			synchronized (peerAddressToNameTable) {
				while ((peerAddressToNameTable.get(addr) == null) && isRunning) {
					try {
						peerAddressToNameTable.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} // end of synchronized block

			if (isRunning) {
				// send our objects to this peer, if we have any
				ControlCenter.getObjectManager().sendObjectsToNewPeer(addr);
	
				// we need to signal that the objects have been sent
				// (in case we didn't have any objects to send)
				send(ControlCenter.NOTIFY, ControlCenter.OBJECTS_HAVE_BEEN_SENT, addr);
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Sent OBJECTS_HAVE_BEEN_SENT to " + addr);
				}
				
				synchronized (ControlCenter.allPeersHaveFinishedSetup) {
					// remove this thread from the list so that it will be garbage collected
					joinThreads.remove(addr);
					// if there are no more threads in the list then signal
					if (joinThreads.size() == 0) {
						ControlCenter.allPeersHaveFinishedSetup.setValue(true);
						ControlCenter.allPeersHaveFinishedSetup.notifyAll();
					}
				} // end synchronized(ControlCenter.allPeersHaveFinishedSetup)
			}
			
			return;
		}
		
		public void stopJoin() {
			synchronized (ControlCenter.allPeersHaveFinishedSetup) {
				// remove this thread from the list so that it will be garbage collected
				joinThreads.remove(addr);
				
				// signal that this thread is dying
				isRunning = false;
				// unstick spaceForMeHasBeenAllocated
				synchronized (spaceForMeHasBeenAllocated) {
					spaceForMeHasBeenAllocated.notifyAll();
				}
				// unstick peerAddressToNameTable
				synchronized (peerAddressToNameTable) {
					peerAddressToNameTable.notifyAll();
				}
				
				// at this point, because isRunning is false, the run() method will skip to the end
				//  there are no more threads in the list then signal
				if (joinThreads.size() == 0) {
					ControlCenter.allPeersHaveFinishedSetup.setValue(true);
					ControlCenter.allPeersHaveFinishedSetup.notifyAll();
				}
			} // end synchronized(ControlCenter.allPeersHaveFinishedSetup)
		}
	}
	

	/**
	 * Member Class PeerFosteringThread, an instance of this thread should be
	 * running at activation time
	 */
	class PeerFosteringThread extends Thread {

		/**
		 * default constructor that starts up the thread
		 */
		public PeerFosteringThread() {
			this.setName("PeerFosteringThread");
			this.start();
		}

		/**
		 * Starts the thread if there are other peers in the system
		 */
		public void run() {
			if (DEBUG_CHANNEL_SETUP) {
				System.out.println("New Peer Fostering Thread started");
			}
			if (noOfPeers.getValue() > 0) { // otherwise we are the first peer
				Address peerToFosterMeAddr = null;
				while (true) {
					synchronized (members) {
						int peerToFosterMeSeq = randomNumGenerator.nextInt(noOfPeers.getValue());
						peerToFosterMeAddr = (Address) members.get(peerToFosterMeSeq);
						if (!peerToFosterMeAddr.equals(channel.getAddress())) {
							members.notifyAll();
							break;
						}
					}
				}
				sendFragMessage("", REQUEST_TO_BE_FOSTERED, peerToFosterMeAddr);

				synchronized (fosterResult) {
					while (fosterResult.getValue() == false) {
						try {
							fosterResult.wait();
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}
				}
			} else { // end if(noOfOtherPeers)
				if (DEBUG_CHANNEL_SETUP) {
					System.out.println("PeerFosteringThread.run(): We are the first peer");
				}
			}
		}
	}

	/**
	 * Instead of using a constructor pm calls its own activate method in the
	 * get instance method. Returns TRUE if the peer has existed before, FALSE
	 * if the peer has not existed before.
	 * 
	 * @return TRUE if the peer has existed before, FALSE if not
	 */
	private boolean activateIsComplete = false;
	public boolean activate() {
		
		Thread activateThread = new Thread() {
			public void run() {
				try {
					channel = new JChannel(props);
					
					channel.addChannelListener(PeerManagerImpl.instance);
					channel.setDiscardOwnMessages(true);
					channel.setReceiver(PeerManagerImpl.instance);
					channel.connect(groupName);
					
					Thread sendMonitor = new Thread() {
						public void run() {
							Message sendMsg;
							try {
								sendMsg = sendQueue.take();
								while (isRunning) {
									if(DEBUG_SENDING){
										System.out.println("Sending message through channel");
									}
									channel.send(sendMsg);
									sendMsg = sendQueue.take();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};

					isRunning = true;
					sendMonitor.start();
					
					//myAddr = channel.getAddress();
					//ControlCenter.getObjectManager().setMyAddress(channel.getAddress());
		
					instance.peerAddressToNameTable.put(channel.getAddress(), instance.getMyPeerName());
					instance.peerNameToAddressTable.put(instance.getMyPeerName(), channel.getAddress());
		
					// wait until viewAccepted has been called at least once.
					// this gives us our initial list of peers
					synchronized (peerListReceived) {
						while (peerListReceived.getValue() == false) {
							try {
								peerListReceived.wait();
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						} // end while
					} // end synchronized(peerSetReceived)
		
					// wait until all peers have finished setup. If we have no peers (after 
					// receiving our first peerList during the section above) then skip this 
					if (noOfPeers.getValue() > 0) {
						synchronized (ControlCenter.allPeersHaveFinishedSetup) {
							while (!ControlCenter.allPeersHaveFinishedSetup.getValue()) {
								try {
									ControlCenter.allPeersHaveFinishedSetup.wait();
								} catch (InterruptedException ex) {
									ex.printStackTrace();
								}
							} // end while
						} // end synchronized(otherPeerCount)
					}
		
					new PeerFosteringThread();
					
					long expiredTime = 0;
					while(!connected && expiredTime < activationTimeOut){
						Thread.sleep(timeBetweenActivationChecks);
						expiredTime += timeBetweenActivationChecks;
					}
					if(!connected){
						throw new RuntimeException("Connection to Channel '" + groupName + "' was not established within " + activationTimeOut + "ms.");
					} else {
						Thread.sleep(additionalWaitAfterConnect);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("Couldn't create JChannel!");
				}
				activateIsComplete = true;
			}
		};
		activateThread.start();
		while (!activateIsComplete) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return receivedMyOwnObjects;
	}

	/**
	 * The entry point to get a reference to the PeerManager implementation of
	 * the singleton pattern. Does not seem to be used by the framework.
	 * 
	 * @return PeerManager instance
	 * @throws StartUpException
	 *             if PeerManager has not been started properly
	 */
	public static PeerManager getInstance() throws StartUpException {
		if (instance == null) {
			throw new StartUpException("PeerManager doesn't exist");
		}
		return instance;
	}

	/**
	 * Called by ControlCenter to start up the PeerManager
	 * 
	 * @return PeerManager instance
	 * @throws StartUpException
	 */
	public static PeerManager startPeerManager(String groupName, String peerName)
			throws StartUpException {
		if (instance != null) {
			throw new StartUpException("PeerManager has already been started");
		}
		instance = new PeerManagerImpl(groupName, peerName);
		return instance;
	}

	public static void stopPeerManager() {
		isRunning = false;
		instance.channel.close();
		instance = null;
	}

	/*
	 * -------------- Interface PeerManager --------------
	 */

	/**
	 * Maps a peer name to a peer address.
	 * 
	 * @param name
	 *            name of the peer
	 */
	public Address getPeerAddress(String name) {
		Address addr = (Address)peerNameToAddressTable.get(name);
		// peerNameToAddress can be indirectly accessed from outside FragMe using this method.
		// It is possible for an application to access peers through the ObjectManager before their
		// name-to-address mapping has been initialized. This cannot happen at startup - only when
		// a peer joins and is then referenced before it has transmitted it's name.
		if (addr == null)
			throw new RuntimeException("A peer name-to-addr mapping doesn't exist - probably not fully registered yet.");
		return addr;
	}

	/**
	 * Maps a peer address to a peer name.
	 * 
	 * @param addr
	 *            address of the peer
	 */
	public String getPeerName(Address addr) {
		String name = (String) peerAddressToNameTable.get(addr);

		// peerAddressToNameTable can be indirectly accessed from outside FragMe using this method.
		// It is possible for an application to access peers through the ObjectManager before their
		// addr-to-name mapping has been initialised. This cannot happen at startup - only when
		// a peer joins and is then referenced before it has transmitted it's name.
		if (name == null)
			throw new RuntimeException("A peer addr-to-name mapping doesn't exist - probably not fully registered yet.");
		return name;
	}

	/**
	 * Receives change from the channel,and pass it on to the OM.
	 * 
	 * @param object
	 *            changed object
	 */
	public void receive(FMeObject object, Address fromAddress) {
		if (object.getOwnerAddr().equals(channel.getAddress()))
			receivedMyOwnObjects = true;
		ControlCenter.getObjectManager().receiveChange(object, fromAddress);
	}

	/**
	 * Sends a message formed by a performative field, the content of the
	 * message and the address to send to (use null for multicasting)
	 * 
	 * @param performative
	 *            the performative field
	 * @param objectToSend
	 *            content of the message
	 * @param addr
	 *            the address to send to (use null for multicasting)
	 */
	private static final FragMessage sendFragMsg = new FragMessage();
	public synchronized void send(String performative, Object objectToSend, Address addr) {
		Serializable serialised = null;
		if (objectToSend instanceof FMeObject) {
			if(DEBUG_SENDING){
				System.out.println("Object to send is instance of FMeObject");
			}
			serialised = (FMeObject) objectToSend;
		} else {
			if (objectToSend instanceof Serializable) {
				if(DEBUG_SENDING){
					System.out.println("Object to send is instance of Serializable");
				}
				serialised = (Serializable) objectToSend;
			} else {
				throw new RuntimeException(
						"Object asked to be sent through PM is not serializable");
			}
		}
		sendFragMsg.setContent(serialised);
		sendFragMsg.setPerformative(performative);
		Message sendMsg = new Message(addr, channel.getAddress(), sendFragMsg);
		serialised = null;
		objectToSend = null;

		try {
			sendQueue.put(sendMsg);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Sets the message content and type of mc and sends it to the address
	 * parsed in.
	 * 
	 * @param content
	 *            content of message container mc
	 * @param messageType
	 *            type of message container mc
	 * @param addr
	 *            address to send to (use null for multicasting)
	 */
	public void sendFragMessage(Object content, String messageType, Address addr) {
		mc.setContent(content);
		mc.setMessageType(messageType);
		send(ControlCenter.NOTIFY, mc, addr);
	}

	/*
	 * -------------- End Interface PeerManager --------------
	 */

	/*
	 * --------------Interface MessageListener --------------
	 */

	/**
	 * Empty implementation - do not use
	 */
	public byte[] getState() {
		System.out.println("EMPTY IMPLEMENTATION -- DO NOT USE");
		byte[] b = null;
		return b;
	}

	/**
	 * Empty implementation - do not use
	 */
	public void setState(byte B[]) {
		System.out.println("EMPTY IMPLEMENTATION -- DO NOT USE");
	}

	/**
	 * Implementation of the MessageListener Interface
	 * 
	 * @param msg
	 *            the message received
	 */
	public synchronized void receive(Message msg) {
		Address senderAddr = msg.getSrc();
		String s = senderAddr.toString();
		String a = channel.getAddress().toString();
		// if it's not our own object(that means we need to care)
		// shouldn't happen because we adjusted the protocol options so that
		// changes
		if (s.equals(a)) {
			senderAddr = null;
			return;
		}
		FragMessage afragMsg = (FragMessage) msg.getObject();
		String performative = afragMsg.getPerformative();
		Object content = afragMsg.getContent();

		if (performative.equals(ControlCenter.MODIFY)) {
			receiveModify(content, senderAddr);
		} else if (performative.equals(ControlCenter.MODIFY_FAILED)) {
			receiveModifyFailed(content, senderAddr);
		} else if (performative.equals(ControlCenter.REQUEST_DELETE)) {
			receiveRequestDelete(content, senderAddr);	
		} else if (performative.equals(ControlCenter.DELETE)) {
			receiveDelete(content, senderAddr);
		} else if (performative.equals(ControlCenter.REQUEST_DELETE_FAILED)) {
			receiveRequestDeleteFailed(content, senderAddr);
		} else if (performative.equals(ControlCenter.REQUEST_DELEGATE_OWNERSHIP)) {
			receiveRequestDelegateOwnership(content, senderAddr);
		} else if (performative.equals(ControlCenter.DELEGATED_OWNERSHIP)) {
			receiveDelegatedOwnership(content, senderAddr);
		} else if (performative.equals(ControlCenter.REQUEST_DELEGATE_OWNERSHIP_FAILED)) {
			receiveRequestOwnershipFailed(content, senderAddr);
		} else if (performative.equals(ControlCenter.NOTIFY)) {
			receiveNotify(content, senderAddr);
		}

		senderAddr = null;
		afragMsg = null;
		performative = null;
		content = null;
	}

	/**
	 * Receive a change made to an object.
	 * The object could belong to us (in which case check first) or to another peer.
	 * If the object is not already in the object manager then it will be added.
	 * @param content The object that is modified or an object 
	 * reflection (reference to object, field and new value)
	 */
	private void receiveModify(Object content, Address senderAddr) {
		if (content instanceof FMeObject) {
			// It is an instance of FMeObject
			if(DEBUG_SENDING){
				System.out.println("Receive: FMeObject");
			}
			
			// Try to find the object in the object manager
			FMeObject serialised = (FMeObject) content;
			FMeObject existObject = ControlCenter.getObjectManager().lookup(serialised);
			
			// Decide if this should be performed
			if ((existObject != null) &&
					existObject.getOwnerAddr().equals(channel.getAddress()) &&
					!existObject.askChangeHandlersAllowChange(serialised, ControlCenter.getPeerName(senderAddr))) {
				send(ControlCenter.MODIFY_FAILED, existObject, senderAddr);
				return;
			}

			// Deserialize
			FMeObject receivedObject = FragMeFactory.deserialize(serialised);
			receive(receivedObject, senderAddr);
			
			// Inform observers 
			if (existObject == null) {
				informNewFMeObjectObservers(receivedObject);
			}
			receivedObject.informChangeObserversChanged();
			
		} else if (content instanceof FMeObjectReflection) {
			// It is an instance of FMeObjectReflection
			if(DEBUG_SENDING){
				System.out.println("Receive: FMeObjectReflection");
			}
			
			// Try to find the object in the object manager
			FMeObjectReflection refObject = (FMeObjectReflection) content;
			FMeObject existObject = ControlCenter.getObjectManager().lookupById(refObject.getId());

			if (existObject == null) {
				// This should not be possible
				throw new RuntimeException("FMeObjectReflection received for object that was not found");
			} else {
				// Decide if it should be performed
				if (existObject.getOwnerAddr().equals(channel.getAddress()) &&
						!existObject.askChangeHandlersAllowChangeField(refObject, ControlCenter.getPeerName(senderAddr))) {
					send(ControlCenter.MODIFY_FAILED, existObject, senderAddr);
					return;
				}
				
				// Deserialize (call the set method for the specified field)
				if (existObject.askChangeHandlersAllowChangeField(refObject, ControlCenter.getPeerName(senderAddr))) {
					String tempFieldName = refObject.getFieldName().substring(0, 1).toUpperCase() +
							refObject.getFieldName().substring(1, refObject.getFieldName().length());
					Method m;
					try {
						// HACK
						existObject.frameworkChanging(true);
						m = existObject.getClass().getMethod("set" + tempFieldName,
								new Class[] { refObject.getValueObject().getClass() });
						m.invoke(existObject,
								new Object[] { refObject.getValueObject() });
						existObject.frameworkChanging(false);
						// HACK
						existObject.informChangeObserversChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Received a modify failed message from an object owner for a change we
	 * tried to perform.
	 * @param content The object that we tried to change, with values according to the owner
	 * @param senderAddr The sender of the message
	 */
	private void receiveModifyFailed(Object content, Address senderAddr) {
		// It is an instance of FMeObject
		if(DEBUG_SENDING){
			System.out.println("Receive: FMeObject");
		}
		
		// Try to find the object in the object manager (it should be there in this case)
		FMeObject serialised = (FMeObject) content;
		FMeObject existObject = ControlCenter.getObjectManager().lookup(serialised);
		
		// Decide if this should be performed
		if (existObject == null) {
			throw new RuntimeException("Modify failed received for object that was not found");
		}

		// Deserialize
		FMeObject receivedObject = FragMeFactory.deserialize(serialised);
		receive(receivedObject, senderAddr);
		
		// Inform observers
		receivedObject.informChangeHandlersChangeFailed();
	}
	
	/**
	 * Received a request to delete an object which we (should) own
	 * @param content The object that requests delete
	 * @param senderAddr
	 */
	private void receiveRequestDelete(Object content, Address senderAddr) {
		String senderName = ControlCenter.getPeerName(senderAddr);
		FMeObject obj = ControlCenter.getObjectManager().lookupById((String)content);
		if (obj.getOwnerAddr().equals(channel.getAddress())) { 
			if (obj.askDeleteHandlersAllowDelete(senderName)) {
				obj.informChangeObserversDeleted();
				ControlCenter.getObjectManager().deleteObject(channel.getAddress(), (String)content);
			} else {
				send(ControlCenter.REQUEST_DELETE_FAILED, content, senderAddr);
			}
		} else {
			send(ControlCenter.REQUEST_DELETE_FAILED, content, senderAddr);
		}
	}

	/**
	 * Receive an object deletion
	 * @param content The object deleted
	 * @param senderAddr The address of the sender
	 */
	private void receiveDelete(Object content, Address senderAddr) {
		FMeObject obj = ControlCenter.getObjectManager().lookupById((String)content);
		if (obj.getOwnerAddr().equals(senderAddr)) { 
			obj.informChangeObserversDeleted();
			ControlCenter.getObjectManager().deleteObject(senderAddr, (String)content);
		}
	}

	/**
	 * Receive notification that a request to delete an un-owned object was rejected by the owner
	 * @param content The object that we were trying to delete
	 * @param senderAddr The sender of the message
	 */
	private void receiveRequestDeleteFailed(Object content, Address senderAddr) {
		FMeObject obj = ControlCenter.getObjectManager().lookupById((String)content);
		obj.informDeleteHandlersDeleteFailed();
	}

	/**
	 * Received a request to delegate ownership for an object that we (should) own.
	 * If successful then tell the object to delegate itself.
	 * @param content The object to change ownership
	 * @param senderAddr The address of the requester
	 */
	private void receiveRequestDelegateOwnership(Object content, Address senderAddr) {
		String senderName = ControlCenter.getPeerName(senderAddr);
		FMeObject obj = ControlCenter.getObjectManager().lookupById((String)content);
		if (obj.getOwnerAddr().equals(channel.getAddress())) { 
			if (obj.askDelegateHandlersAllowDelegateOwnership(senderName)) {
				// Tell the object to delegate ownership 
				obj.delegateOwnership(senderName);
			} else {
				send(ControlCenter.REQUEST_DELEGATE_OWNERSHIP_FAILED, content, senderAddr);
			}
		} else {
			send(ControlCenter.REQUEST_DELEGATE_OWNERSHIP_FAILED, content, senderAddr);
		}
	}

	/**
	 * We received a notification saying that an object has been delegated ownership.
	 * Update the object manager
	 * @param content The object to change ownership
	 * @param senderAddr The address of the sender
	 */
	private void receiveDelegatedOwnership(Object content, Address senderAddr) {
		FMeObjectReflection objRef = (FMeObjectReflection)content;
		FMeObject obj = ControlCenter.getObjectManager().lookupById(objRef.getId());
		if (obj.getOwnerAddr().equals(senderAddr)) {
			ControlCenter.getObjectManager().delegatedOwnership((Address)objRef.getValueObject(), obj);
			obj.informChangeObserversDelegatedOwnership();
		}
	}

	/**
	 * A request for delegation failed, so inform the listeners.
	 * @param content 
	 * @param senderAddr The address of the sender (original owner)
	 */
	private void receiveRequestOwnershipFailed(Object content, Address senderAddr) {
		FMeObject obj = ControlCenter.getObjectManager().lookupById((String)content);
		if (obj.getOwnerAddr().equals(senderAddr)) {
			obj.informDelegateHandlersDelegateOwnershipFailed();
		}
	}

	/**
	 * 
	 * @param content
	 *            the object that receives notify
	 * @param senderAddr
	 *            the address of the sender
	 */
	private void receiveNotify(Object content, Address senderAddr) {
		if (content instanceof MessageContainer) {
			String commandType = ((MessageContainer) content).getMessageType();
			Object msgContent = ((MessageContainer) content).getContent();
			
			if (commandType.equals(PEER_NAME)) {
				
				synchronized (peerAddressToNameTable) {
					if(DEBUG_SYNCHRONIZATION){
						System.out.println("Received PEER_NAME from " + senderAddr);
					}
					// add the peer name to the table
					if (peerAddressToNameTable.get(senderAddr) == null) {
						peerAddressToNameTable.put(senderAddr, (String)msgContent);
						peerNameToAddressTable.put((String)msgContent, senderAddr);
					} else {
						System.err.println("Warning: received PEER_NAME duplicate (perhaps a new peer is using the address of an old peer)");
					}
					// notify (even if we did not add to the table, the address might be being reused)
					peerAddressToNameTable.notifyAll();
				}
				
			} else if (commandType.equals(REPLY_TO_BE_FOSTERED)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received REPLY_TO_BE_FOSTERED from " + senderAddr);
				}
				peerImFostering = (Address) msgContent;
				sendFragMessage("", REQUEST_TO_FOSTER, peerImFostering);
				
			} else if (commandType.equals(REPLY_TO_FOSTER)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received REPLY_TO_FOSTER from " + senderAddr);
				}
				synchronized (fosterResult) {
					peerMyChildIsFostering = (Address) msgContent;
					fosterResult.setValue(true);
					fosterResult.notifyAll();
				}
				
			} else if (commandType.equals(REQUEST_TO_BE_FOSTERED)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received REQUEST_TO_BE_FOSTERED from " + senderAddr);
				}
				if (peerImFostering != null) {
					sendFragMessage(peerImFostering, REPLY_TO_BE_FOSTERED, senderAddr);
					peerMyChildIsFostering = peerImFostering;
					peerImFostering = senderAddr;
				} else {
					sendFragMessage(channel.getAddress(), REPLY_TO_BE_FOSTERED, senderAddr);
					peerMyChildIsFostering = channel.getAddress();
					peerImFostering = senderAddr;
				}
				
			} else if (commandType.equals(REQUEST_TO_FOSTER)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received REQUEST_TO_FOSTER from " + senderAddr);
				}
				sendFragMessage(peerImFostering, REPLY_TO_FOSTER, senderAddr);
				
			}
			commandType = null;
			msgContent = null;
		} else {
			String notification = (String) content;
			if (notification.equals(ControlCenter.OBJECTS_HAVE_BEEN_SENT)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received OBJECTS_HAVE_BEEN_SENT from " + senderAddr);
				}
				// record this peer in the table and notify that it has been updated
				synchronized (peersWhichHaveSentObjects) {
					if (peersWhichHaveSentObjects.get(senderAddr) == null) {
						peersWhichHaveSentObjects.put(senderAddr, new FlagBool(true));
					}
					peersWhichHaveSentObjects.notifyAll();
				} // end synchronized(otherPeersHaveMyObjectsCount)
				
			} else if (notification.equals(ControlCenter.REQUEST_PEER_NAME)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received REQUEST_PEER_NAME from " + senderAddr);
				}
				// send our peer name
				sendFragMessage(peerName, PEER_NAME, senderAddr);
				
			} else if (notification.equals(ControlCenter.SPACE_HAS_BEEN_ALLOCATED)) {
				
				if(DEBUG_SYNCHRONIZATION){
					System.out.println("Received SPACE_ALLOCATED_FOR_NEW_PEER from " + senderAddr);
				}
				// 
				synchronized (spaceForMeHasBeenAllocated) {
					if (spaceForMeHasBeenAllocated.get(senderAddr) == null) {
						spaceForMeHasBeenAllocated.put(senderAddr, new FlagBool(true));
					}
					spaceForMeHasBeenAllocated.notifyAll();
				} // end synchronized(spaceAllocated)
				
			}
		}
	}

	/*
	 * -------------- End Interface MessageListener --------------
	 */

	/*
	 * -------------- Interface MembershipListener --------------
	 */

	/**
	 * Implementation of the MemberShipListener Interface.
	 * 
	 * @param new_view
	 *            View
	 */
	public synchronized void viewAccepted(View new_view) {
		ArrayList<Address> joined_mbrs;
		ArrayList<Address> left_mbrs;
		List<Address> tmp;
		Address tmp_mbr; // IpAddress
		if(DEBUG_CHANNEL_SETUP){
			if(new_view instanceof MergeView){
			    System.out.println("** MergeView=" + new_view);
			} else
		        System.out.println("** View=" + new_view);
		}
		
		if (new_view == null)
			return;
		tmp = new_view.getMembers(); // Collection of IpAddress

		synchronized (members) {

			// get new members
			joined_mbrs = new ArrayList<Address>();
			for (int i = 0; i < tmp.size(); i++) {
				tmp_mbr = tmp.get(i);
				if (!members.contains(tmp_mbr))
					joined_mbrs.add(tmp_mbr);
			}

			// get members that left
			left_mbrs = new ArrayList<Address>();
			for (int i = 0; i < members.size(); i++) {
				tmp_mbr = members.get(i);
				if (!tmp.contains(tmp_mbr))
					left_mbrs.add(tmp_mbr);
			}

			// adjust our own membership
			members.clear();
			members.addAll(tmp);

			// record how many peers (excluding ourselves) we have (for fast lookup)
			noOfPeers.setValue(members.size() - 1);
		} // end synchronized(members)

		synchronized (peerListReceived) {
			peerListReceived.setValue(true);
			peerListReceived.notifyAll();
		} // end synchronized(peerListReceived)

		if (joined_mbrs.size() > 0)
			for (int i = 0; i < joined_mbrs.size(); i++)
				memberJoined(joined_mbrs.get(i));

		if (left_mbrs.size() > 0)
			for (int i = 0; i < left_mbrs.size(); i++)
				memberLeft(left_mbrs.get(i));
	}

	/**
	 * Empty implementation of MemberShipListener Interface.
	 * 
	 * @param suspected_mbr
	 *            Address
	 */
	public void suspect(Address suspected_mbr) {
	}

	/**
	 * Empty implementation of MemberShipListener Interface.
	 */
	public void block() {
	}

	/*
	 * -------------- End of Interface MembershipListener --------------
	 */

	/**
	 * Private method called by viewAccepted() when new member joins.
	 * 
	 * @param addr
	 *            address of the new member joined
	 */
	private void memberJoined(Address addr) {
		if (!addr.equals(channel.getAddress())) {
			System.out.println("Member " + addr + " joined!");
			// update ControlCenter.allPeersHaveFinishedSetup to indicate that there is 
			// a peer which has not completed setup and start a new thread to 
			// join to this address
			synchronized (ControlCenter.allPeersHaveFinishedSetup) {
				ControlCenter.allPeersHaveFinishedSetup.setValue(false);
				ControlCenter.allPeersHaveFinishedSetup.notifyAll();
				joinThreads.put(addr, new JoinThread(addr));
			}
		}
	}

	/**
	 * Private method called by viewAccepted() when member left.
	 * 
	 * @param addr
	 *            address of the member that left
	 */
	private void memberLeft(Address addr) {
		System.out.println("Member " + addr + " left!");
		// if we are still trying to establish a connection to this peer then stop the thread
		JoinThread joinThread = joinThreads.get(addr);
		if (joinThread != null) {
			System.err.println("Killing synchronization thread for member " + addr + " (that did not fully synchronize).");
			joinThread.stopJoin();
			joinThread = null;
		}
		if (addr.equals(peerImFostering)) {
			peerImFostering = peerMyChildIsFostering;
			if (!peerImFostering.equals(channel.getAddress())) {
				fosterResult.setValue(false);
				sendFragMessage("", REQUEST_TO_FOSTER, peerImFostering);
			}
			ControlCenter.getObjectManager().delegatePeerObjects(addr);
		} else {
			ControlCenter.getObjectManager().deletePeer(addr);
		}
		//this.setChanged();
		//this.notifyObservers(new Object());
	}

	/**
	 * Returns my peer name.
	 * 
	 * @return my peer name
	 */
	public String getMyPeerName() {
		return peerName;
	}

	/**
	 * Returns group name.
	 * 
	 * @return group name
	 */
	public String getGroupName() {
		return groupName;
	}

	//@Override
	public void channelConnected(Channel channel) {
		if(DEBUG_CHANNEL_SETUP){
			System.out.println("Channel '" + groupName + "' connected.");
		}
		connected = true;
	}

	//@Override
	public void channelDisconnected(Channel channel) {
		if(DEBUG_CHANNEL_SETUP){
			System.out.println("Channel '" + groupName + "' disconnected.");
		}
		connected = false;
	}

	//@Override
	public void channelClosed(Channel channel) {
		if(DEBUG_CHANNEL_SETUP){
			System.out.println("Channel '" + groupName + "' closed.");
		}
		connected = false;
	}

	//@Override
	public void channelShunned() {
		if(DEBUG_CHANNEL_SETUP){
			System.out.println("Channel '" + groupName + "' shunned.");
		}
	}

	//@Override
	public void channelReconnected(Address addr) {
		if(DEBUG_CHANNEL_SETUP){
			System.out.println("Channel '" + groupName + "' reconnected.");
		}
		connected = true;
	}

	@Override
	public void getState(OutputStream output) throws Exception {
	}

	@Override
	public void setState(InputStream input) throws Exception {
	}

	@Override
	public void unblock() {
	}

}

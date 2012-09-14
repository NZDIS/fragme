package org.nzdis.fragme.peers;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Random;
import java.util.Vector;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.ObjectManager;
import org.nzdis.fragme.PeerManager;
import org.nzdis.fragme.exceptions.StartUpException;
import org.nzdis.fragme.factory.FragMeFactory;
import org.nzdis.fragme.objects.FMeObject;
import org.nzdis.fragme.objects.FMeObjectReflection;
import org.nzdis.fragme.peers.TypeWrappers.FlagBool;
import org.nzdis.fragme.peers.TypeWrappers.FlagInt;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;
import org.jgroups.blocks.PullPushAdapter;

/**
 * Implementation of the PeerManager Interface
 * 
 * @author Mengqiu Wang, Heiko Wolf
 * @refactored Morgan Bruce, Frank Wu 1/8/2008
 */
public class PeerManagerImpl extends Observable implements PeerManager,
		MessageListener, MembershipListener, ChannelListener {
	
	private static boolean debug = ControlCenter.debug;

	/** the singleton instance of PeerManagerImpl */
	private static PeerManagerImpl instance;
	public static final String FACTORY_ID = "FACTORY_ID";
	public static final String PEER_NAME = "PEER_NAME";
	public static final String REPLY_TO_FOSTER = "REPLY_TO_FOSTER";
	public static final String REPLY_TO_BE_FOSTERED = "REPLY_TO_BE_FOSTERED";
	public static final String REQUEST_TO_FOSTER = "REQUEST_TO_FOSTER";
	public static final String REQUEST_TO_BE_FOSTERED = "REQUEST_TO_BE_FOSTERED";

	/** Member variables */
	private boolean receiveMyOwnObjects = false;
	private FlagBool peerListReceived = new FlagBool(false);
	private FlagInt otherPeersHaveMyObjectsCount = new FlagInt(0);
	private static FlagInt noOfPeers = null;
	//private Vector threads = new Vector();

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
	private Vector members = new Vector();
	/** The address of the current peer */
	private Address myAddr;
	private Hashtable spaceAllocated = new Hashtable();
	private Hashtable peerAddressToNameTable = new Hashtable();
	private HashMap<Address, JoinThread> joinThreads = new HashMap<Address, JoinThread>(); 
	private MessageContainer mc = new MessageContainer();
	//init inside send()
	//private FragMessage fragMsg = new FragMessage();

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
	
	/** the PushPullAdaptor used */
	private PullPushAdapter adaptor;

	/** protocol stack */
	private static String props = "UDP(mcast_addr=224.0.0.0;mcast_port=7500;ip_ttl=32;"
			+ "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):"
			+ "PING(timeout=2000;num_initial_members=3):"
			+ "FD_SOCK:"
			+ "VERIFY_SUSPECT(timeout=1000):"
			+ "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):"
			+ "UNICAST(timeout=5000):"
			+ "pbcast.STABLE(desired_avg_gossip=20000):"
			+ "FRAG(frag_size=8096;down_thread=false;up_thread=false):"
			+ "CAUSAL:"
			+ "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;"
			+ "shun=false;print_local_addr=true)";

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
			// first send my peerName to other peer
			sendFragMessage(peerName, PEER_NAME, addr);
			System.out.println("Sent PEER_NAME to " + addr);
			// Request peer name early to prevent the situation where the application wants the name for 
			// an unnamed peer (from peerAddressToNameTable). This cannot happen at startup - it only
			// might happen when a new peer has joined, after it has had space allocated and before it's
			// name has been resolved.
			//send(ControlCenter.NOTIFY, ControlCenter.REQUEST_PEER_NAME, addr);

			// allocate space for the new peer
			ControlCenter.getObjectManager().allocateSpaceForPeer(addr);
			send(ControlCenter.NOTIFY, ControlCenter.SPACE_ALLOCATED_FOR_NEW_PEER, addr);
			System.out.println("Sent SPACE_ALLOCATED_FOR_NEW_PEER to " + addr);

			// wait until we are told that the new peer has allocated space for us
			synchronized (spaceAllocated) {
				while (spaceAllocated.get(addr) == null) {
					try {
						//printStatus("spaceAllocated " + addr + " wait...", addr);
						spaceAllocated.wait();
						//printStatus("spaceAllocated " + addr + " finished", addr);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} // end of synchronized block
			
			// after the above block of code,space has been allocated for us by
			// the new peer

			// make sure we get the peer's name before we send objects to him
			synchronized (peerAddressToNameTable) {
				while (peerAddressToNameTable.get(addr) == null) {
					try {
						//printStatus("peerAddressToNameTable " + addr + " wait...", addr);
						peerAddressToNameTable.wait(); // This might be notified by a different incoming address to name
						//printStatus("peerAddressToNameTable " + addr + " finished", addr);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			ControlCenter.getObjectManager().sendObjectsToNewPeer(addr);

			// TODO
			//printStatus("sent ControlCenter.OBJECT_SENT_TO_NEW_PEER to " + addr, addr);
			send(ControlCenter.NOTIFY, ControlCenter.OBJECT_SENT_TO_NEW_PEER, addr);
			System.out.println("Sent OBJECT_SENT_TO_NEW_PEER to " + addr);

			synchronized (ControlCenter.flag) {
				int prev_v = ControlCenter.flag.getValue();
				ControlCenter.flag.setValue(prev_v + 1);
				ControlCenter.flag.notifyAll();
				joinThreads.remove(addr);
			} // end synchronized(ControlCenter.flag)
			return;
		}
		
		public void kill() {
			synchronized (otherPeersHaveMyObjectsCount) {
				otherPeersHaveMyObjectsCount.notifyAll();
			}
			synchronized (ControlCenter.flag) {
				System.err.println("Killing JoinThread that has not finished");
				ControlCenter.flag.notifyAll();
				joinThreads.remove(addr);
			} // end synchronized(ControlCenter.flag)
		}
	}
	
	private synchronized void printStatus(String s, Address addr) {
		System.out.printf("   Status (" + addr + "):\n");
		System.out.printf("    - %s\n", s);
		System.out.printf("    - ControlCenter.flag: %d\n", ((FlagInt)ControlCenter.flag).getValue());
		System.out.printf("    - noOfOtherPeers: %d\n", this.noOfPeers);
		System.out.printf("    - otherPeerCount: %d\n", otherPeersHaveMyObjectsCount.getValue());
		System.out.printf("    - members.size(): %d\n", members.size());
		System.out.printf("    - spaceAllocated.size(): %d\n", spaceAllocated.size());
		System.out.printf("    - peerAddressToNameTable.size(): %s\n", peerAddressToNameTable.size());
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
			System.out.println("New Peer Fostering Thread started");
			if (noOfPeers.getValue() > 0) { // otherwise we are the first peer
				Address peerToFosterMeAddr = null;
				while (true) {
					synchronized (members) {
						int peerToFosterMeSeq = randomNumGenerator.nextInt(noOfPeers.getValue());
						peerToFosterMeAddr = (Address) members.get(peerToFosterMeSeq);
						if (!peerToFosterMeAddr.equals(myAddr)) {
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
			} // end if(noOfOtherPeers)
		}
	}

	/**
	 * Instead of using a constructor pm calls its own activate method in the
	 * get instance method. Returns TRUE if the peer has existed before, FALSE
	 * if the peer has not existed before.
	 * 
	 * @return TRUE if the peer has existed before, FALSE if not
	 */
	public boolean activate() {
		try {
			channel = new JChannel(props);
			channel.setChannelListener(this);
			channel.setOpt(Channel.LOCAL, new Boolean(false));
			channel.connect(groupName);

			// activates the PullPushAdapter with MessageListener and
			// MembershipListener
			adaptor = new PullPushAdapter(channel, this, this);

			myAddr = channel.getLocalAddress();
			ControlCenter.getObjectManager().setMyAddress(myAddr);
			instance.peerAddressToNameTable.put(myAddr, instance.getMyPeerName());

			// wait until viewAccepted has been called at least once - this gives us our
			// initial list of peers
			synchronized (peerListReceived) {
				while (peerListReceived.getValue() == false) {
					try {
						peerListReceived.wait();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				} // end while
			} // end synchronized(peerSetReceived)

			/* broadcasting its peer name to other peers */
			//sendFragMessage(peerName, PEER_NAME, null);
			
			// TODO
			synchronized (otherPeersHaveMyObjectsCount) {
				while (otherPeersHaveMyObjectsCount.getValue() < noOfPeers.getValue()) {
					try {
						//printStatus("waiting to receive objects from all peers ...", myAddr);
						otherPeersHaveMyObjectsCount.wait();
						//printStatus("waiting to receive objects from all peers finished", myAddr);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				} // end while
			} // end synchronized(otherPeerCount)

			PeerFosteringThread newThread = new PeerFosteringThread();
			//threads.add(newThread);
			
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

		return receiveMyOwnObjects;
		// peerFosterForNewPeer();

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
		instance.channel.close();
		instance = null;
	}

	/*
	 * -------------- Interface PeerManager --------------
	 */

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
	public void receive(FMeObject object) {
		if (object.getOwnerAddr().equals(myAddr))
			receiveMyOwnObjects = true;
		ControlCenter.getObjectManager().receiveChange(object);
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
	public synchronized void send(String performative, Object objectToSend, Address addr) {
		Serializable serialised = null;
		if (objectToSend instanceof FMeObject) {
			if(debug){
				System.out.println("Object to send is instance of FMeObject");
			}
			serialised = (FMeObject) objectToSend;
		} else {
			if (objectToSend instanceof Serializable) {
				if(debug){
					System.out.println("Object to send is instance of Serializable");
				}
				serialised = (Serializable) objectToSend;
			} else {
				throw new RuntimeException(
						"Object asked to be sent through PM is not serializable");
			}
		}
		FragMessage fragMsg = new FragMessage();
		fragMsg.setContent(serialised);
		fragMsg.setPerformative(performative);
		Message msg = new Message(addr, myAddr, fragMsg);
		try {
			if(debug){
				System.out.println("Sending message through channel");
			}
			channel.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		serialised = null;
		objectToSend = null;

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
		String a = myAddr.toString();
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

		if (performative.equals(ControlCenter.SYNCHRONIZE)) {
			receiveSynchronise(content);
		} else if (performative.equals(ControlCenter.MODIFY)) {
			receiveModify(content);
		} else if (performative.equals(ControlCenter.DELETE)) {
			receiveDelete(content, senderAddr);
		} else if (performative.equals(ControlCenter.REQUEST_DELETE)) {
			receiveRequestDelete(content);
		} else if (performative.equals(ControlCenter.NOTIFY)) {
			receiveNotify(content, senderAddr);
		}

		senderAddr = null;
		afragMsg = null;
		performative = null;
		content = null;

	}

	/**
	 * 
	 * @param content
	 *            an object synchronised
	 */
	private void receiveSynchronise(Object content) {
		Integer factoryStartingSeq = (Integer) content;
		FragMeFactory.objectIdCounter = factoryStartingSeq.intValue();
	}

	/**
	 * 
	 * @param content
	 *            an object modified
	 */
	private void receiveModify(Object content) {
		if (content instanceof FMeObject) {
			if(debug){
				System.out.println("Receive: FMeObject");
			}
			FMeObject serialised = (FMeObject) content;
			FMeObject receivedObject = FragMeFactory.deserialize(serialised);
			serialised = null;
			receive(receivedObject);
			receivedObject.changedObject();
			this.setChanged();
			this.notifyObservers(receivedObject);
			receivedObject = null;
		} else if (content instanceof FMeObjectReflection) {
			if(debug){
				System.out.println("Receive: FMeObjectReflection");
			}
			FMeObjectReflection refObject = (FMeObjectReflection) content;

			ObjectManager OM = ControlCenter.getObjectManager();

			FMeObject existObject = OM.lookupById(refObject.getId());
			String tempFieldName = refObject.getFieldName().substring(0, 1)
					.toUpperCase()
					+ refObject.getFieldName().substring(1,
							refObject.getFieldName().length());
			Method m;
			try {
				// HACK
				if (existObject == null) {
					return;
				}
				existObject.frameworkChanging(true);
				m = existObject.getClass().getMethod("set" + tempFieldName,
						new Class[] { refObject.getValueObject().getClass() });
				m.invoke(existObject,
						new Object[] { refObject.getValueObject() });
				existObject.frameworkChanging(false);
				// HACK
				existObject.changedObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param content
	 *            the object deleted
	 * @param senderAddr
	 *            the address of the sender
	 */
	private void receiveDelete(Object content, Address senderAddr) {
		ControlCenter.getObjectManager().deleteObject(senderAddr,
				((Integer) content).intValue());
	}

	/**
	 * 
	 * @param content
	 *            the object that requests delete
	 * 
	 */
	private void receiveRequestDelete(Object content) {
		ControlCenter.getObjectManager().deleteObject(myAddr,
				((Integer) content).intValue());
	}

	/**
	 * 
	 * @param content
	 *            the object that receives notify
	 * @param senderAddr
	 *            the address of the sender
	 */
	private void receiveNotify(Object content, Address senderAddr) {
		// this notification is the reply of a REQUEST_TO_BE_FOSTERED or
		// REQUEST_TO_FOSTER notification
		if (content instanceof MessageContainer) {
			String commandType = ((MessageContainer) content).getMessageType();
			Object msgContent = ((MessageContainer) content).getContent();
			// this is a PEER_NAME notification
			if (commandType.equals(PEER_NAME)) {
				synchronized (peerAddressToNameTable) {
					System.out.println("Received PEER_NAME from " + senderAddr);
					if (peerAddressToNameTable.get(senderAddr) == null) {
						peerAddressToNameTable.put(senderAddr, msgContent);
					} else {
						System.err.println("Warning: received PEER_NAME duplicate (perhaps a new peer is using the address of an old peer)");
					}
					peerAddressToNameTable.notifyAll();
				}
			} else if (commandType.equals(REPLY_TO_BE_FOSTERED)) {
				// this is a FOSTER_REQUEST_DENIED notification
				peerImFostering = (Address) msgContent;
				sendFragMessage("", REQUEST_TO_FOSTER, peerImFostering);
			} else if (commandType.equals(REPLY_TO_FOSTER)) {
				System.out.println("received REPLY_TO_FOSTER");
				synchronized (fosterResult) {
					peerMyChildIsFostering = (Address) msgContent;
					fosterResult.setValue(true);
					fosterResult.notifyAll();
				}
			} else if (commandType.equals(REQUEST_TO_BE_FOSTERED)) {
				if (peerImFostering != null) {
					sendFragMessage(peerImFostering, REPLY_TO_BE_FOSTERED, senderAddr);
					peerMyChildIsFostering = peerImFostering;
					peerImFostering = senderAddr;
				} else {
					sendFragMessage(myAddr, REPLY_TO_BE_FOSTERED, senderAddr);
					peerMyChildIsFostering = myAddr;
					peerImFostering = senderAddr;
				}
			} else if (commandType.equals(REQUEST_TO_FOSTER)) {
				System.out.println("received REQUEST_TO_FOSTER");
				sendFragMessage(peerImFostering, REPLY_TO_FOSTER, senderAddr);
			}
			commandType = null;
			msgContent = null;
		} else {
			String notification = (String) content;
			// this is an OBJECT_SENT_TO_NEW_PEER notification
			if (notification.equals(ControlCenter.OBJECT_SENT_TO_NEW_PEER)) {
				System.out.println("Received OBJECT_SENT_TO_NEW_PEER from " + senderAddr);
				// TODO
				synchronized (otherPeersHaveMyObjectsCount) {
					int prev_val = otherPeersHaveMyObjectsCount.getValue();
					otherPeersHaveMyObjectsCount.setValue(prev_val + 1);
					otherPeersHaveMyObjectsCount.notifyAll();
				} // end synchronized(otherPeersHaveMyObjectsCount)
			} else if (notification.equals(ControlCenter.REQUEST_PEER_NAME)) {
				// this is a REQUEST_PEER_NAME notification
				sendFragMessage(peerName, PEER_NAME, senderAddr);
			} else if (notification.equals(ControlCenter.SPACE_ALLOCATED_FOR_NEW_PEER)) {
				System.out.println("Received SPACE_ALLOCATED_FOR_NEW_PEER from " + senderAddr);
				// this is a SPACE_ALLOCATED_FOR_NEW_PEER notification
				synchronized (spaceAllocated) {
					if (spaceAllocated.get(senderAddr) == null) {
						spaceAllocated.put(senderAddr, new FlagBool(true));
						spaceAllocated.notifyAll();
					}
				} // end synchronized(spaceAllocated)
				// TODO 
			} // end else if (notification.equals(...))
		} // if else (content instanceof Address)
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
		Vector joined_mbrs, left_mbrs, tmp;
		Object tmp_mbr; // IpAddress

		if (new_view == null)
			return;
		tmp = new_view.getMembers(); // Collection of IpAddress

		synchronized (members) {

			// get new members
			joined_mbrs = new Vector();
			for (int i = 0; i < tmp.size(); i++) {
				tmp_mbr = tmp.elementAt(i);
				if (!members.contains(tmp_mbr))
					joined_mbrs.addElement(tmp_mbr);
			}

			// get members that left
			left_mbrs = new Vector();
			for (int i = 0; i < members.size(); i++) {
				tmp_mbr = members.elementAt(i);
				if (!tmp.contains(tmp_mbr))
					left_mbrs.addElement(tmp_mbr);
			}

			// adjust our own membership
			members.removeAllElements();
			members.addAll(tmp);

			noOfPeers = new FlagInt(members.size() - 1);
		} // end synchronized(members)

		synchronized (peerListReceived) {
			peerListReceived.setValue(true);
			peerListReceived.notifyAll();
		} // end synchronized(peerListReceived)

		if (joined_mbrs.size() > 0)
			for (int i = 0; i < joined_mbrs.size(); i++)
				memberJoined((Address) joined_mbrs.elementAt(i));

		if (left_mbrs.size() > 0)
			for (int i = 0; i < left_mbrs.size(); i++)
				memberLeft((Address) left_mbrs.elementAt(i));
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
		if (!addr.equals(myAddr)) {
			System.out.println("Member " + addr + " joined!");
			JoinThread newJoinThread = new JoinThread(addr);
			// TO-DO later try not to hold reference to the thread
			joinThreads.put(addr, newJoinThread);
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
		if (joinThreads.containsKey(addr)) {
			System.err.println("Killing thread");
			JoinThread joinThread = joinThreads.get(addr);
			joinThread.kill();
		}
		if (addr.equals(peerImFostering)) {
			peerImFostering = peerMyChildIsFostering;
			if (!peerImFostering.equals(myAddr)) {
				fosterResult.setValue(false);
				sendFragMessage("", REQUEST_TO_FOSTER, peerImFostering);
			}
			ControlCenter.getObjectManager().delegatePeerObjects(addr);
		} else {
			ControlCenter.getObjectManager().deletePeer(addr);
		}
		this.setChanged();
		this.notifyObservers(new Object());
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

	@Override
	public void channelConnected(Channel channel) {
		if(debug){
			System.out.println("Channel '" + groupName + "' connected.");
		}
		connected = true;
	}

	@Override
	public void channelDisconnected(Channel channel) {
		if(debug){
			System.out.println("Channel '" + groupName + "' disconnected.");
		}
		connected = false;
	}

	@Override
	public void channelClosed(Channel channel) {
		if(debug){
			System.out.println("Channel '" + groupName + "' closed.");
		}
		connected = false;
	}

	@Override
	public void channelShunned() {
		if(debug){
			System.out.println("Channel '" + groupName + "' shunned.");
		}
	}

	@Override
	public void channelReconnected(Address addr) {
		if(debug){
			System.out.println("Channel '" + groupName + "' reconnected.");
		}
		connected = true;
	}
}

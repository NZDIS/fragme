package org.nzdis.fragme.peers;

import java.io.Serializable;
import java.lang.reflect.Method;
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
		MessageListener, MembershipListener {
	
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
	private FlagBool viewAcceptedCalled = new FlagBool(false);
	private FlagInt otherPeerCount = new FlagInt(0);
	private static FlagInt noOfExistingPeer = null;
	private int noOfOtherPeers = 0;
	private Vector threads = new Vector();

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
	private MessageContainer mc = new MessageContainer();
	private FragMessage fragMsg = new FragMessage();

	/** The transport channel used */
	private JChannel channel;

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
	public static FlagInt getNoOfExistingPeer() {
		return noOfExistingPeer;
	}

	/**
	 * Member Class CheckThread, an instance of this thread should be running at
	 * initialization time
	 */
	class CheckThread extends Thread {
		Address addr;

		/**
		 * Constructor that parses in an address, and starts up the thread
		 * 
		 * @param addr
		 *            the address of a peer
		 */
		public CheckThread(Address addr) {
			this.addr = addr;
			this.start();
		}

		/**
		 * Starts up the thread by sending the peerName to other peers and
		 * allocate space for the peer
		 */
		public void run() {
			// first send my peerName to other peer
			sendFragMessage(peerName, PEER_NAME, addr);

			// allocate space for the new peer
			FlagBool flag = (FlagBool) spaceAllocated.get(addr);
			if (flag == null) {
				flag = new FlagBool(false);
				spaceAllocated.put(addr, flag);
			}

			ControlCenter.getObjectManager().allocateSpaceForPeer(addr);
			send(ControlCenter.NOTIFY,
					ControlCenter.SPACE_ALLOCATED_FOR_NEW_PEER, addr);

			synchronized (flag) {
				while (flag.getValue() == false) {
					try {
						flag.wait();
					} catch (InterruptedException ex) {
						System.out.println("Interrupted");
					}
				} // end while
			} // end of synchronized block
			// after the above block of code,space has been allocated for us by
			// the new peer

			// make sure we get the peer's name before we send objects to him
			synchronized (peerAddressToNameTable) {
				while (peerAddressToNameTable.get(addr) == null) {
					send(ControlCenter.NOTIFY, ControlCenter.REQUEST_PEER_NAME,
							addr);
					try {
						peerAddressToNameTable.wait();
					} catch (InterruptedException exx) {
						System.out.println("Interrupted");
					}
				}
				ControlCenter.getObjectManager().sendObjectsToNewPeer(addr);
			}

			send(ControlCenter.NOTIFY, ControlCenter.OBJECT_SENT_TO_NEW_PEER,
					addr);

			synchronized (ControlCenter.flag) {
				int prev_v = ControlCenter.flag.getValue();
				ControlCenter.flag.setValue(prev_v + 1);
				ControlCenter.flag.notifyAll();
			} // end synchronized(ControlCenter.flag)
			return;
		}
	}

	/**
	 * Member Class PeerFosteringThread, an instance of this thread should be
	 * running at activation time
	 */
	class PeerFosteringThread extends Thread {
		Address addr;

		/**
		 * default constructor that starts up the thread
		 */
		public PeerFosteringThread() {
			this.start();
		}

		/**
		 * Starts the thread if there are other peers in the system
		 */
		public void run() {
			System.out.println("New Peer Fostering Thread started");
			if (noOfOtherPeers > 0) { // otherwise we are the first peer
				Address peerToFosterMeAddr = null;
				while (true) {
					int peerToFosterMeSeq = randomNumGenerator
							.nextInt(noOfOtherPeers);
					synchronized (members) {
						peerToFosterMeAddr = (Address) members
								.get(peerToFosterMeSeq);
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
			channel.setOpt(Channel.LOCAL, new Boolean(false));
			channel.connect(groupName);

			// activates the PullPushAdapter with MessageListener and
			// MembershipListener
			adaptor = new PullPushAdapter(channel, this, this);

			myAddr = channel.getLocalAddress();
			ControlCenter.getObjectManager().setMyAddress(myAddr);
			instance.peerAddressToNameTable.put(myAddr, instance
					.getMyPeerName());

			synchronized (viewAcceptedCalled) {
				while (viewAcceptedCalled.getValue() == false) {
					try {
						viewAcceptedCalled.wait();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				} // end while
			} // end synchronized(viewAcceptedCalled)

			/* broadcasting its peer name to other peers */
			sendFragMessage(peerName, PEER_NAME, null);

			synchronized (otherPeerCount) {
				while (otherPeerCount.getValue() < noOfOtherPeers) {
					try {
						otherPeerCount.wait();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				} // end while
			} // end synchronized(otherPeerCount)

			PeerFosteringThread newThread = new PeerFosteringThread();
			threads.add(newThread);
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

		if (name == null)
			throw new RuntimeException(
					"A peer addr-to-name mapping doesn't exist");
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
	public void send(String performative, Object objectToSend, Address addr) {
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
	public void receive(Message msg) {
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
					if (peerAddressToNameTable.get(senderAddr) == null) {
						peerAddressToNameTable.put(senderAddr, msgContent);
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
					sendFragMessage(peerImFostering, REPLY_TO_BE_FOSTERED,
							senderAddr);
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
				synchronized (otherPeerCount) {
					int prev_val = otherPeerCount.getValue();
					otherPeerCount.setValue(prev_val + 1);
					otherPeerCount.notifyAll();
				} // end synchronized(otherPeerCount)
			} else if (notification.equals(ControlCenter.REQUEST_PEER_NAME)) {
				// this is a REQUEST_PEER_NAME notification
				sendFragMessage(peerName, PEER_NAME, senderAddr);
			} else if (notification
					.equals(ControlCenter.SPACE_ALLOCATED_FOR_NEW_PEER)) {
				// this is a SPACE_ALLOCATED_FOR_NEW_PEER notification
				FlagBool flag = (FlagBool) spaceAllocated.get(senderAddr);
				if (flag == null) {
					flag = new FlagBool(false);
					spaceAllocated.put(senderAddr, flag);
				}
				synchronized (flag) {
					flag.setValue(true);
					flag.notifyAll();
				} // end synchronized(flag)
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
	public void viewAccepted(View new_view) {
		Vector joined_mbrs, left_mbrs, tmp;
		Object tmp_mbr;

		if (new_view == null)
			return;
		tmp = new_view.getMembers();
		noOfOtherPeers = tmp.size() - 1;

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
		} // end synchronized(members)

		noOfExistingPeer = new FlagInt(members.size() - 1);

		synchronized (viewAcceptedCalled) {
			viewAcceptedCalled.setValue(true);
			viewAcceptedCalled.notifyAll();
		} // end synchronized(viewAcceptedCalled)

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
			CheckThread newThread = new CheckThread(addr);
			// TO-DO later try not to hold reference to the thread
			threads.add(newThread);
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
}

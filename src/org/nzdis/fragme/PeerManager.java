package org.nzdis.fragme;

import org.nzdis.fragme.objects.FMeObject;
import org.jgroups.Address;

/**
 * PeerManager Interface
 * 
 * @author Heiko Wolf, Mengqiu Wang
 * 
 */
public interface PeerManager {

	/**
	 * push the change of an object to other peers,called by ObjectManager
	 */
	public abstract void send(String performative, Object objectToSend,
			Address addr);

	/**
	 * receive an object change from other peers,calls ObjectManager's
	 * receiveChange()
	 */
	public abstract void receive(FMeObject object, Address fromAddress);

	/**
	 * maps a peer address to a peer name
	 */
	public abstract String getPeerName(Address addr);

	/**
	 * returns the name of the group
	 */
	public abstract String getGroupName();

	/**
	 * activate a PeerManager returns a boolean indicating whether this peer
	 * still exists in the game
	 */
	public abstract boolean activate();

	/**
	 * Gets the peer's name
	 * 
	 * @return the peer's name
	 */
	public abstract String getMyPeerName();

}

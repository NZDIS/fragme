package org.nzdis.fragme;

import org.nzdis.fragme.objects.Message;

/**
 * The Message Receiver Interface - called by MessageManager
 */
public interface MessageReciever {

	/**
	 * Called by MessageManager
	 * 
	 * @param message
	 *            Message to be received
	 */
	public abstract void msgRecievedEvent(Message message);
}

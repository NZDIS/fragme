package org.nzdis.fragme.testMessageSending;

import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.MessageReceiver;
import org.nzdis.fragme.objects.Message;

/**
 * Sets up a connection and allow the peer to receive messages.
 * 
 * @author Frank Wu
 *
 */
public class Client1 extends Thread implements MessageReceiver {
	/**
	 * Sets up a new connection and allow it to receive messages
	 */
	public void run() {
		ControlCenter.setUpConnections("test", "c1");
		ControlCenter.receiveMessages(this);
		System.out.println("SEND NOW (run Client2.java)");
	}
	/**
	 * Sets the TestMessageSending.msg field
	 * 
	 * @param message the message to be sent
	 */
	public void msgReceivedEvent(Message message) {
		TestMessageSending.msg = message;
	}

	/**
	 * Returns the message received
	 * 
	 * @param message
	 * @return the message received
	 */
	public Message msgReceived(Message message) {
		return message;
	}
}

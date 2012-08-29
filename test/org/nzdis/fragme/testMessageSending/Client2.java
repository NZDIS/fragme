package org.nzdis.fragme.testMessageSending;

import java.util.Vector;

import org.nzdis.fragme.ControlCenter;

/**
 * Sets up a new client that sends messages
 * 
 * @author Frank Wu
 *
 */
public class Client2 {
	/**
	 * Connects to the framework and sends messages to other peers
	 * 
	 * @param args is not used
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws InterruptedException {
		ControlCenter.setUpConnections("test", "c2");
		Vector recipients = new Vector();
		recipients.add("c1");

		/**
		 * send message - uncomment and run below for testSendMessage (comment
		 * out the other 3 messages)
		 */
		ControlCenter.sendMessage("hello");
		/**
		 * send message with recipient - uncomment and run below for
		 * testSendMessageWithRecepient (comment out the other two messages
		 * below)
		 */
		// ControlCenter.sendMessage(recipients, "hello");
		/**
		 * send message with type - uncomment and run below for
		 * testSendMessageWithType (comment out the last message below)
		 */
		// ControlCenter.sendMessage("hello", "type1");
		/**
		 * send message with recipients and type - uncomment and run below for
		 * testSendMessageWithRecepientAndType
		 */
		// ControlCenter.sendMessage(recipients,"hello","type1");
		
		System.out.println("message sent");
		ControlCenter.closeUpConnections();
		System.exit(0);
	}
}

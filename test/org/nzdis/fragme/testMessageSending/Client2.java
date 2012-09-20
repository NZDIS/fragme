package org.nzdis.fragme.testMessageSending;

import java.util.Vector;

import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.objects.Message;

/**
 * Sets up a new client that sends messages - to be compiled as second instance 
 * to test message passing.
 * 
 * @author Frank Wu
 * @author Christopher Frantz - revised for automated testing
 *
 */
public class Client2 {
	
	private static final Integer startupDelay = 5000;
	private static final Integer shutdownDelay = 5000;
	
	/**
	 * Connects to the framework and sends messages to other peers
	 * 
	 * @param args is not used
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws InterruptedException {
		new Message();
		ControlCenter.setUpConnections("test", "c2");
		Vector recipients = new Vector();
		recipients.add("c1");
		String testName = null;
		if(args != null && args.length > 0){
			testName = args[0];
		}
		//System.out.println("waiting for " + delay + "ms");
		Thread.sleep(startupDelay);
		//System.out.println(delay + "ms up");
		if(testName != null){
			if(testName.equals("testSendMessage")){
				/**
				 * send message
				 */
				ControlCenter.sendMessage("hello");
				System.out.println("Sent message for test " + testName);
			} else if(testName.equals("testSendMessageWithRecipient")){
				/**
				 * send message with recipient
				 */
				ControlCenter.sendMessage(recipients, "hello");
				System.out.println("Sent message for test " + testName);
			} else if(testName.equals("testSendMessageWithType")){
				/**
				 * send message with type
				 */
				ControlCenter.sendMessage("hello", "type1");
				System.out.println("Sent message for test " + testName);
			} else if(testName.equals("testSendMessageWithRecipientAndType")){
				/**
				 * send message with recipients and type
				 */
				ControlCenter.sendMessage(recipients,"hello","type1");
				System.out.println("Sent message for test " + testName);
			} else {
				System.out.println("Send nothing for test " + testName);
				//shutdown after 3 seconds
			}
		} else {
			System.out.println("No valid test parameter specified");
		}
		ControlCenter.closeUpConnections();
		Thread.sleep(shutdownDelay);
		//System.exit(0);
	}
}

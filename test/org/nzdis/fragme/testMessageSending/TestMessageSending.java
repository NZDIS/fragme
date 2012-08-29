package org.nzdis.fragme.testMessageSending;

import java.io.IOException;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.objects.Message;
import junit.framework.TestCase;

/**
 * This class tests the message sending functionalities implemented in
 * ControlCentre that sends messages from one peer to another.
 * 
 * @author Frank Wu
 * 
 */
public class TestMessageSending extends TestCase {

	/**
	 * Sets up a new connection for each test case. 
	 */
	public void setUp() {
		// ControlCenter.setUpConnections("testGroup1", "testPeer");
	}

	/**
	 * Closes the connection at the end of each test case.
	 */
	public void tearDown() {
		ControlCenter.closeUpConnections();
	}

	// public void testGetMessageObject() {
	// assertNotNull(ControlCenter.getMessageObject());
	// }

	public static Message msg = null;

	/**
	 * Tests sendMessage(String content)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void testSendMessage() throws InterruptedException, IOException {
		// ControlCenter.closeUpConnections();
		Client1 c1 = new Client1();
		c1.start();
		System.out.println("wait 10s");
		Thread.sleep(10000);
		System.out.println("10s up");
		// try {
		// Runtime.getRuntime().exec(
		// "java -jar S:\\Eclipse33\\fragme\\src\\tests\\test.jar");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		System.out.println("wait 10s");
		Thread.sleep(10000);
		System.out.println("10s up");
		assertEquals("The content of the message received is incorrect",
				"hello", msg.getContent());
		assertEquals("The sender of the message received is incorrect", "c2",
				msg.getSender());
		assertEquals("The type of the message received is incorrect", null, msg
				.getType());
		assertEquals("The recipients of the message received is incorrect",
				null, msg.getRecipients());
	}

	/**
	 * Tests sendMessage(Vector recipients, String content)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void testSendMessageWithRecipient() throws InterruptedException,
			IOException {
		Client1 c1 = new Client1();
		c1.start();
		Thread.sleep(10000);
		assertEquals("The content of the message received is incorrect",
				"hello", msg.getContent());
		assertEquals("The sender of the message received is incorrect", "c2",
				msg.getSender());
		assertEquals("The type of the message received is incorrect", null, msg
				.getType());
		assertEquals("The recipients of the message received is incorrect",
				"c1", msg.getRecipients().get(0));
	}

	/**
	 * Tests sendMessage(String content, String type)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void testSendMessageWithType() throws InterruptedException,
			IOException {
		Client1 c1 = new Client1();
		c1.start();
		Thread.sleep(10000);
		assertEquals("The content of the message received is incorrect",
				"hello", msg.getContent());
		assertEquals("The sender of the message received is incorrect", "c2",
				msg.getSender());
		assertEquals("The type of the message received is incorrect", "type1",
				msg.getType());
		assertEquals("The recipients of the message received is incorrect",
				null, msg.getRecipients());
	}

	/**
	 * Tests sendMessage(Vector recipients, String content, String type)
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void testSendMessageWithRecipientAndType()
			throws InterruptedException, IOException {
		Client1 c1 = new Client1();
		c1.start();
		Thread.sleep(10000);
		assertEquals("The content of the message received is incorrect",
				"hello", msg.getContent());
		assertEquals("The sender of the message received is incorrect", "c2",
				msg.getSender());
		assertEquals("The type of the message received is incorrect", "type1",
				msg.getType());
		assertEquals("The recipients of the message received is incorrect",
				"c1", msg.getRecipients().get(0));
	}
}

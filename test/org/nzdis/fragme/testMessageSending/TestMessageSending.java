package org.nzdis.fragme.testMessageSending;

import java.io.IOException;
import org.nzdis.fragme.ControlCenter;
import org.nzdis.fragme.objects.Message;
import org.nzdis.fragme.testControlCenter.TestControlCenter;
import org.nzdis.fragme.util.DetermineOS;
import junit.framework.TestCase;

/**
 * This class tests the message sending functionalities implemented in
 * ControlCenter that sends messages from one peer to another.
 * 
 * @author Frank Wu
 * @author Christopher Frantz - revised to automate tests (and automatically generate pathnames for external jar).
 * 
 */
public class TestMessageSending extends TestCase {

	static final Integer delay = 8000;
	static String path = "java -jar ";
	
	static {
		/*
		 * class for testing with multiple peers should be regenerated when changing code base. 
		 * Export of entire framework project (i.e. FragME and JGroups) as Runnable Jar with TestClass.java as main class
		 */
		String subPath = TestMessageSending.class.getResource("Client2.jar").getPath();
		if(DetermineOS.getOS().equals(DetermineOS.WINDOWS)){
			subPath = subPath.substring(1);
			path = "cmd /C start " + path;
		} else if(DetermineOS.getOS().equals(DetermineOS.LINUX)){
			path += "-Djava.net.preferIPv4Stack=true ";
		}
		path += subPath;
		System.out.println("Path to multiple peer test jar file: " + path);
		//ensure that message class is loaded into FragMe factory
		new Message();
	}
	
	/**
	 * Sets up a new connection for each test case. 
	 */
	public void setUp() {
		//reset message
		msg = null;
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
		Client1 c1 = new Client1();
		c1.start();
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
		Runtime.getRuntime().exec(path + " testSendMessage");
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
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
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
		Runtime.getRuntime().exec(path + " testSendMessageWithRecipient > out.txt");
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
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
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
		Runtime.getRuntime().exec(path + " testSendMessageWithType");
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
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
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
		Runtime.getRuntime().exec(path + " testSendMessageWithRecipientAndType");
		System.out.println("waiting for " + delay + "ms");
		Thread.sleep(delay);
		System.out.println(delay + "ms up");
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

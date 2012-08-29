package org.nzdis.fragme.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Feng, Ning, Abdulla
 * 
 */
public class Sender {
	private Socket clientSocket;

	private static final int RECEIVER_PORT = 9999;

	private static final int TALKING_PORT = 9998;

	private static FragMeFile fragmeFile = null;

	private static String message = null;

	private static String receiverIp = null;

	public Sender() {
		// nothing needed here
	}

	public void attach(FragMeFile f, String receiverIp) {
		Sender.fragmeFile = f;
		Sender.receiverIp = receiverIp;
		sendFile();
		Sender.fragmeFile = null;
		Sender.receiverIp = null;
	}

	public void sendFile() {
		send(fragmeFile, receiverIp);
	}

	public void attach(String message, String receiverIp) {
		Sender.message = message;
		Sender.receiverIp = receiverIp;
		sendMessage();
		Sender.message = null;
		Sender.receiverIp = null;

	}

	public void sendMessage(){
		send(message, receiverIp);
	}

	private void send(String message, String receiverIpAddress) {
		try {
			clientSocket = new Socket(receiverIpAddress, TALKING_PORT);

			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(clientSocket.getOutputStream()));

			os.writeObject((Object) message);

			os.flush();

			os.close();

			System.out.println("Message Sent!");
		} catch (UnknownHostException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(FragMeFile file, String receiverIpAddress) {

		try {
			clientSocket = new Socket(receiverIpAddress, RECEIVER_PORT);

			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(clientSocket.getOutputStream()));

			os.writeObject(file);

			os.flush();

			os.close();

			System.out.println("File Sent!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package org.nzdis.fragme.otago.p2p;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 
 * @author Feng, Ning, Abdulla
 * 
 */
public class Sender{
	private Socket clientSocket;
	private static final int RECEIVER_PORT=9999;
	
	public Sender(Object fileToReceiver,String toIP){
		try {
			send(fileToReceiver, toIP, RECEIVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(Object fileToReceiver, String receiverIpAddress, int receiverPort)throws IOException {	
		
			clientSocket = new Socket(receiverIpAddress, receiverPort);
			
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
		
			os.writeObject(fileToReceiver);
			
			os.flush();
			
			os.close();
			
			System.out.println("Sent!");
		}
}

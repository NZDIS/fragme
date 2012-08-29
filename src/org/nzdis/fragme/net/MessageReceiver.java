package org.nzdis.fragme.net;

/**
 * A simple multithreaded server. With a callback.
 */

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author Feng,Ning,Abdulla
 * 
 */
public class MessageReceiver {
	private static final String RECEIVER_PORT = "9998";

	static ServerSocket srvSock = null;

	private boolean alive = false;
	
	public synchronized boolean isAlive() {
		return this.alive;
	}

	public synchronized void setAlive(boolean alive) {
		this.alive = alive;
	}

	public MessageReceiver() {
		this(null);
	}

	public MessageReceiver(MessageCallback mc) {
		System.err.println("Message Receiver started!");
		alive = true;
		int port = Integer.parseInt(RECEIVER_PORT);
		try {
			if ((srvSock == null) || (srvSock.isClosed())){
				srvSock = new ServerSocket(port);	
			}			
			while (alive) {
				Socket connection = srvSock.accept();
				(new MessageWorker(connection, mc)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void finalize() {
		if (srvSock != null) {
			try {
				srvSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			srvSock = null;
		}
	}
}

class MessageWorker extends Thread {

	Socket sock = null;

	MessageCallback callback = null;

	MessageWorker(Socket s, MessageCallback mc) {
		sock = s;
		callback = mc;
	}

	public void run() {
		try {
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(sock.getInputStream()));
			try {
				Object out = is.readObject();
				if (out instanceof String) {
					String f = ((String) out);

					if (callback == null) {
						System.err.println("Message:" + f);
					} else {
						callback.receiveMessage(f);
					}

				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

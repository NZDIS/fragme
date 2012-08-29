package org.nzdis.fragme.net;

/**
 * A simple multithreaded server.
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
public class FileReceiver {

	private static final String RECEIVER_PORT = "9999";

	static ServerSocket srvSock = null;

	public FileReceiver() {
		this(null);
	}

	public FileReceiver(FileCallback fc) {
		System.err.println("File Receiver started!");
		int port = Integer.parseInt(RECEIVER_PORT);
		try {
			if ((srvSock == null) || (srvSock.isClosed())){
				srvSock = new ServerSocket(port);	
			}
			Socket connection = srvSock.accept();
			(new FileWorker(connection, fc)).start();
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

class FileWorker extends Thread {

	Socket sock = null;

	FileCallback callback = null;

	FileWorker(Socket s, FileCallback fc) {
		sock = s;
		callback = fc;
	}

	public void run() {
		try {
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(sock.getInputStream()));
			try {
				Object out = is.readObject();
				if (out instanceof FragMeFile) {
					FragMeFile f = ((FragMeFile) out);
					if (callback == null) {
						System.err.println("File:" + f.getFileName());
						f.writeData();
					} else {
						callback.receiveFile(f);
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

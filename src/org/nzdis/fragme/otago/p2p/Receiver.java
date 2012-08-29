package org.nzdis.fragme.otago.p2p;

/**
 * A simple multithreaded server.
 */
 
import java.io.*;
import java.net.*;

/**
 * 
 * @author Feng,Ning,Abdulla
 *
 */
public class Receiver {
	private static final String RECEIVER_PORT="9999";
	static ServerSocket srvSock = null;
	
	MessageReceiver mr = null;
	
	public Receiver(){
		int port = Integer.parseInt(RECEIVER_PORT);
		try {
			srvSock = new ServerSocket(port);
			while(true) {
				Socket connection = srvSock.accept();
				(new Worker(connection)).start();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Receiver(MessageReceiver mr){
		this.mr = mr;
		int port = Integer.parseInt(RECEIVER_PORT);
		try {
			srvSock = new ServerSocket(port);
			while(true) {
				Socket connection = srvSock.accept();
				(new Worker(connection, mr)).start();
			}
		}
		catch(IOException e) {
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

class Worker extends Thread {
	Socket sock = null;
	MessageReceiver mr = null;
	
	Worker(Socket s) {
		sock = s;
	}
	
	Worker(Socket s, MessageReceiver mr) {
		sock = s;
		this.mr = mr;
	}
	
	public void run() {
		try {
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));
			try {
				Object out=is.readObject();
				if(out instanceof String){					
					String temp=(String) out;
					if (mr == null) {
						System.out.println("Message:"+temp);	
					} else {
						mr.receiveMessage(temp);
					}
				}else if(out instanceof FragMeFile){
					FragMeFile f=((FragMeFile)out);
					System.out.println("File:"+f.getFileName());
					f.writeData();
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			is.close();
			
			System.out.println("Thread finish!");

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
	
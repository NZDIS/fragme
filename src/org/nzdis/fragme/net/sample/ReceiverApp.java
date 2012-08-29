package org.nzdis.fragme.net.sample;

import org.nzdis.fragme.net.FileReceiver;
import org.nzdis.fragme.net.MessageReceiver;

public class ReceiverApp {
	public static void main(String[] args) {
		
		new Thread(){
			public void run() {
				FileReceiver fr=new FileReceiver();
			}
		}.start();
		
		new Thread() {
			public void run() {
			MessageReceiver mr=new MessageReceiver();
			}
		}.start();
	}
}

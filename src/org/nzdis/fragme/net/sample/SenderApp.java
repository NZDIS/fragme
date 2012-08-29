package org.nzdis.fragme.net.sample;

import org.nzdis.fragme.net.FragMeFile;
import org.nzdis.fragme.net.Sender;


public class SenderApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Sender s=new Sender();
		
		FragMeFile f=new FragMeFile("1.jpg");
		FragMeFile f2=new FragMeFile("2.jpg");
		
		s.attach(f, "127.0.0.1");
		s.sendFile();
		
		s.attach("Hello!", "127.0.0.1");
		s.sendMessage();
		
		s.attach("YES BABY!", "127.0.0.1");
		s.sendMessage();
		
		s.attach(f2, "127.0.0.1");
		s.sendFile();
	}
}

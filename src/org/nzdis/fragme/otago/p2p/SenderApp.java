package org.nzdis.fragme.otago.p2p;


public class SenderApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FragMeFile f1=new FragMeFile("2.jpg");
		FragMeFile f2=new FragMeFile("Try.mp3");
		Sender s1=new Sender(f2,"192.168.10.1");
		Sender s2=new Sender(f1,"192.168.10.1");
	}

}

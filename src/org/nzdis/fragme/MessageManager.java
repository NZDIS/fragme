package org.nzdis.fragme;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import org.nzdis.fragme.objects.Message;

public class MessageManager implements Observer {

	MessageReceiver actionObject;

	public MessageManager(Object o) {
		actionObject = (MessageReceiver) o;
	}

	public void update(Observable o, Object arg) {

		PeerManager PM = ControlCenter.getPeerManager();

		String myName = PM.getMyPeerName();

		if (o instanceof Message) {
			Message msg = (Message) o;

			Vector recipients = msg.getRecipients();

			if (recipients == null || recipients.contains(myName)) {
				actionObject.msgReceivedEvent(msg);
			}
		}
	}
}
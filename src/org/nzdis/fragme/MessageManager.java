package org.nzdis.fragme;

import java.util.Vector;

import org.nzdis.fragme.objects.ChangeObserver;
import org.nzdis.fragme.objects.FMeObject;
import org.nzdis.fragme.objects.Message;

public class MessageManager implements ChangeObserver {

	MessageReceiver actionObject;

	public MessageManager(Object o) {
		actionObject = (MessageReceiver) o;
	}

	@Override
	public void changed(FMeObject object) {
		if (object instanceof Message) {
			Message msg = (Message) object;

			Vector recipients = msg.getRecipients();

			if (recipients == null || recipients.contains(ControlCenter.getMyName())) {
				actionObject.msgReceivedEvent(msg);
			}
		}
	}

	@Override
	public void delegatedOwnership(FMeObject object) {
	}

	@Override
	public void deleted(FMeObject object) {
	}
}

package org.nzdis.fragme.objects;

import java.io.Serializable;
import java.util.Vector;
import org.nzdis.fragme.factory.FactoryObject;
import org.nzdis.fragme.factory.FragMeFactory;

/**
 * 
 * This class defines the Fragme version of the Message object.
 * 
 */
public class Message extends FMeObject implements Serializable {

	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String sender = new String();

	private Vector recipients = new Vector();

	private String content = new String();

	private String type = new String();

	/**
	 * This method sets the attributes for the message String.
	 * 
	 * 
	 * @param sender
	 *            String, recipients String, type short, content String,
	 *            timeslot Timeslot
	 * @return Void
	 */
	public void setAll(String sender, Vector recipients, String content,
			String type) {
		this.sender = sender;
		this.recipients = recipients;
		this.content = content;
		this.type = type;
	}

	/**
	 * Prevents String construction outside of this class.
	 */
	public Message() {

	}

	/**
	 * Defines a factory class for the Message String
	 */
	private static class Factory extends FragMeFactory {
		protected FactoryObject create() {
			return new Message();
		}
	}

	/**
	 * Registers the factory with the framework
	 */
	static {
		FragMeFactory.addFactory(new Factory(), Message.class);
	}

	/**
	 * This method is used by the FragMeFactory to build an FMeSerialized String
	 * ser from a normal FMeString String.
	 * 
	 * @return Class the class name of the serialized object
	 */
	public Class getSerializedObjectClassName() {
		return Message.class;
	}

	/**
	 * This method is called whenever a serialized String containing the data of
	 * this FMeString is needed. This represents the other end of the network
	 * communication.
	 * 
	 * @param serString
	 *            The serialized String to deserialize.
	 */
	public void deserialize(FMeObject serString) {
		// copy data from serString to this String
		this.sender = ((Message) serString).getSender();
		this.recipients = ((Message) serString).getRecipients();
		this.content = ((Message) serString).getContent();
		this.type = ((Message) serString).getType();
	}

	/**
	 * A generic method that is used when String has changed (this usually just
	 * notifies observers)
	 */
	public void changedObject() {
		// notify observers
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * A generic method that is used when String is deleted - Unimplemented
	 */
	public void deletedObject() {
	}

	/**
	 * Returns the sender
	 * 
	 * @return The sender
	 */
	public String getSender() {
		return this.sender;
	}

	/**
	 * Sets the sender
	 * 
	 * @param sender
	 *            The name of the sender
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * Returns the recipients
	 * 
	 * @return The recipients
	 */
	public Vector getRecipients() {
		return this.recipients;
	}

	/**
	 * Sets the recipients
	 * 
	 * @param recipients
	 *            A vector of recipients
	 */
	public void setRecipients(Vector recipients) {
		this.recipients = recipients;
	}

	/**
	 * Returns the content of the message
	 * 
	 * @return The content of the message
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * Sets the content of the message
	 * 
	 * @param content
	 *            The content of the message
	 */
	public void setMessage(String content) {
		this.content = content;
	}

	/**
	 * Returns the type of the message
	 * 
	 * @return The type of the message
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the sender
	 * 
	 * @param sender
	 *            The sender
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Method that overides the default toString method - for more usefaul
	 * textual display of the message details.
	 * 
	 * @return a String with sender, recipients, type and content
	 */
	public String toString() {
		return "Sender:" + this.sender + " Recipients:" + this.recipients
				+ " Type:" + this.type + " Content:" + this.content;
	}
}

package org.nzdis.fragme.peers;

import java.io.Serializable;

/**
 * <p>
 * Title: MessageContainer
 * </p>
 * <p>
 * Description: a class that contains the content of a message
 * </p>
 * 
 * @author Mengqiu Wang, Heiko Wolf
 * @version 1.0
 * 
 * @refactored Morgan Bruce 1/8/2008
 */
public class MessageContainer implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private String type;
	private Object content;

	/**
	 * Returns the message type
	 * 
	 * @return the message type in String
	 */
	public String getMessageType() {
		return this.type;
	}

	/**
	 * Returns the content
	 * 
	 * @return the content
	 */
	public Object getContent() {
		return this.content;
	}

	/**
	 * Sets the message type field
	 * 
	 * @param type
	 *            the message type
	 * 
	 */
	public void setMessageType(String type) {
		this.type = type;
	}

	/**
	 * Sets the content field
	 * 
	 * @param content
	 *            the content of the object
	 * 
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * Returns a description of the messageContainer
	 * 
	 * @return the description of the messageContainer in String
	 */
	public String toString() {
		return "type " + this.type + " content " + this.content;
	}

}
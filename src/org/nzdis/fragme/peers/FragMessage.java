package org.nzdis.fragme.peers;

import java.io.Serializable;

/**
 * Nested top-level class FragMessage A message used between PeerManagers, it
 * contains a performative field, the content of the message.
 * 
 * @refactored Morgan Bruce 1/8/2008
 */
public class FragMessage implements Serializable {
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private String performative;
	private Object content;

	/**
	 * Sets the performative field
	 * 
	 * @param performative
	 *            sets the performative field
	 */
	public void setPerformative(String performative) {
		this.performative = performative;
	}

	/**
	 * Sets the content field
	 * 
	 * @param content
	 *            sets the content field
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * Returns the content field
	 * 
	 * @return the content field
	 */
	public Object getContent() {
		return this.content;
	}

	/**
	 * Returns the performative field
	 * 
	 * @return the performative field
	 */
	public String getPerformative() {
		return this.performative;
	}

	/**
	 * Returns a description of the FragMessage object
	 * 
	 * @return Description of the FragMessage object
	 */
	public String toString() {
		return "performative " + this.performative + " content " + this.content;
	}
}
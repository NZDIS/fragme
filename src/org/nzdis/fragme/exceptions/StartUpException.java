package org.nzdis.fragme.exceptions;

/**
 * @author Mengqiu Wang, Heiko Wolf
 * 
 * An exception thrown in the start up phase to indicate an improper start up of
 * the PeerManager or ObjectManager
 */
public class StartUpException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Contructor creates the StartUpException of a String message
	 * 
	 * @param msg
	 *            The message of the StartUpException
	 */
	public StartUpException(String msg) {
		super(msg);
	}
}

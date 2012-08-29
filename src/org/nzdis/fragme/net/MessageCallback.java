/**
 * Interface for fragme.net classes to pass messages back to an application.
 */
package org.nzdis.fragme.net;

/**
 * @author barri662
 *
 */
public interface MessageCallback {

	public void receiveMessage(String message);
	
}

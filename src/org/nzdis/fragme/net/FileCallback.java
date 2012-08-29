/**
 * Interface for fragme.net classes to pass files back to an application.
 */
package org.nzdis.fragme.net;

/**
 * @author barri662
 *
 */
public interface FileCallback {

	public void receiveFile(FragMeFile file);
	
}

package org.nzdis.fragme.factory;

import org.jgroups.Address;

/**
 * FactoryObject Interface
 * 
 * @author Mengqiu Wang, Heiko Wolf
 */
public interface FactoryObject {

	/**
	 * Sets the id of the FactoryObject
	 * 
	 * @param id
	 *            Id of the FactoryObject
	 */
	public void setId(int id);

	/**
	 * Returns the FactoryObject Id
	 * 
	 * @return the FactoryObject Id
	 */
	public int getId();

	/**
	 * Returns the address of the owner of the FactoryObject
	 * 
	 * @return the address of the owner of the FactoryObject
	 */
	public Address getOwnerAddr();

	/**
	 * Sets the address of the owner of the FactoryObject
	 * 
	 * @param ownerAddr
	 *            the address of the owner of the FactoryObject
	 */
	public void setOwnerAddr(Address ownerAddr);
}

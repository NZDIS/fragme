package org.nzdis.fragme.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 
 * Class containing network-related functionality to obtain relevant network 
 * address according to user-specified preferences (e.g. interface, protocol type, scope).
 * 
 * @author Nathan D. Lewis 
 * @version 0.1 (20th September 2012)
 *
 */
public class NetworkUtils {

/**
* Returns the address of the current host, which satisfies name, protocol and scope.
*/
    public static final boolean debugGetNonLoopbackAddress = false;
    /** IP protocol versions */
    public static final String IPV4 = "IPV4";
    public static final String IPV6 = "IPV6";
    /** IPv6 scopes */
    public static final String SCOPE_LINK_LOCAL = "SCOPE_LINK_LOCAL";
    public static final String SCOPE_SITE_LOCAL = "SCOPE_SITE_LOCAL";
    public static final String SCOPE_GLOBAL = "SCOPE_GLOBAL";
    /** default interface to be used */
    public static final String DEFAULT_INTERFACE_NAME = null;
    /** default IP protocol to be used */
    public static final String DEFAULT_PROTOCOL = IPV4;
    /** default IPv6 scope to be used (if address type IPV6 is chosen) */
    public static final String DEFAULT_SCOPE = SCOPE_LINK_LOCAL;
    
    /**
     * Gets a non-loopback address, using the default interface protocol and scope
     * @return A non-local loopback address or null if not found. If the device 
     * has more than one then the first valid address will be returned
     */
	public static String getNonLoopBackAddress() {
		return getNonLoopBackAddressByInterfaceName(DEFAULT_INTERFACE_NAME, DEFAULT_PROTOCOL, DEFAULT_SCOPE);
	}

	/**
	 * Gets a non-loopback address of the specified interface protocol, using the default scope
	 * @param protocol The interface protocol 
     * @return A non-local loopback address of the specified type or null if not found. If the device 
     * has more than one then the first valid address will be returned
	 */
	public static String getNonLoopBackAddressByProtocol(String protocol) {
		return getNonLoopBackAddressByInterfaceName(DEFAULT_INTERFACE_NAME, protocol, DEFAULT_SCOPE);
	}

	/**
	 * Gets a non-loopback address of the specified type and scope
	 * @param protocol The interface protocol 
	 * @param scope The scope 
     * @return A non-local loopback address of the specified type and scope or null if not found. If the device 
     * has more than one then the first valid address will be returned
	 */
	public static String getNonLoopBackAddressByProtocol(String protocol, String scope) {
		return getNonLoopBackAddressByInterfaceName(DEFAULT_INTERFACE_NAME, protocol, scope);
	}

	/**
	 * Gets the non-loopback address of the specified name, using the default interface protocol and scope
	 * @param interfaceName The interface name 
     * @return The non-local loopback address of the specified name or null if not found.
	 */
	public static String getNonLoopBackAddressByInterfaceName(String interfaceName) {
		return getNonLoopBackAddressByInterfaceName(interfaceName, DEFAULT_PROTOCOL, DEFAULT_SCOPE);
	}

	/**
	 * Gets the non-loopback address of the specified name and interface protocol, using the default scope
	 * @param interfaceName The interface name 
	 * @param protocol The interface protocol 
     * @return The non-local loopback address of the specified name and type or null if not found.
	 */
	public static String getNonLoopBackAddressByInterfaceName(String interfaceName, String protocol) {
		return getNonLoopBackAddressByInterfaceName(interfaceName, protocol, DEFAULT_SCOPE);
	}

	/**
	 * Gets the non-loopback address of the specified name, using the default interface protocol and scope
	 * @param interfaceName The interface name 
	 * @param protocol The interface protocol 
	 * @param scope The scope 
     * @return The non-local loopback address of the specified name, type and scope or null if not found.
	 */
	public static String getNonLoopBackAddressByInterfaceName(String interfaceName, String protocol, String scope) {
		NetworkInterface intface;

		if (interfaceName != null) { // The programmer has specified an interface name
			try {
				intface = NetworkInterface.getByName(interfaceName);
			} catch (SocketException e) {
				intface = null;
				System.err.println("NetworkInterface " + interfaceName + " was not found!");
				return null;
			}
			return (getNonLoopbackAddress(intface, protocol, scope));
			
		} else { // No interface name specified, so get all interfaces
			Enumeration<NetworkInterface> interfacesEnumerator = null;
			String address = null;
			try {
				interfacesEnumerator = NetworkInterface.getNetworkInterfaces();
				while (interfacesEnumerator.hasMoreElements()) {
					// Check each interface
					intface = interfacesEnumerator.nextElement();
					if (debugGetNonLoopbackAddress) {
						System.out.println("NetworkInterface: " + intface.getDisplayName());
					}
					address = getNonLoopbackAddress(intface, protocol, scope);
					if (address != null) {
						return address;
					}
				}
			} catch (SocketException e) {
				System.err.println("A problem occurred when trying to obtain the list of available NetworkInterfaces.");
				System.err.println("If this occurred on Android then you probably need to add INTERNET permission to your manifest!");
			}
			return null;
		}
	}	
	
	/**
	 * Gets the non-loopback address of the specified network interface, protocol and scope
	 * @param intface The network interface
	 * @param protocol The interface protocol
	 * @param scope The scope
	 * @return The address, or null if not found.
	 */
	private static String getNonLoopbackAddress(NetworkInterface intface, String protocol, String scope){
		Enumeration<InetAddress> inetAddressEnumerator = null;
		InetAddress inetAddress;
		String address = null;
		
		Class<?> protocolClass;
		if (protocol.toUpperCase().equals(IPV4)) {
			protocolClass = Inet4Address.class;
		} else if (protocol.toUpperCase().equals(IPV6)) {
			protocolClass = Inet6Address.class;
		} else {
			protocolClass = null;
		}
		
		for (inetAddressEnumerator = intface.getInetAddresses(); inetAddressEnumerator.hasMoreElements();) {
			inetAddress = inetAddressEnumerator.nextElement();
			if (debugGetNonLoopbackAddress) {
				System.out.println("Network Interface Address " + intface.getName() + " " + inetAddress.getHostAddress().toString() + 
						" is of type " + inetAddress.getClass().toString() + "\n" +
						" Link Local: " + Boolean.toString(inetAddress.isLinkLocalAddress()) + 
						" Site Local: " + Boolean.toString(inetAddress.isSiteLocalAddress()) + 
						" Global: " + Boolean.toString(inetAddress.isMulticastAddress()) );
			}
			if (inetAddress.getClass().equals(protocolClass)) {
				if (protocolClass.equals(Inet4Address.class)) {
					if (!inetAddress.isLoopbackAddress()) {
						address = inetAddress.getHostAddress().toString();
					}
				} else if (protocolClass.equals(Inet6Address.class)) {
					if ( (inetAddress.isLinkLocalAddress() && scope.equals(SCOPE_LINK_LOCAL)) ||
						 (inetAddress.isSiteLocalAddress() && scope.equals(SCOPE_SITE_LOCAL)) ||
						 (inetAddress.isMulticastAddress() && scope.equals(SCOPE_GLOBAL)) ) {
						address = inetAddress.getHostAddress().toString();
					}
				}
			}
		}
		return address;
	}
}

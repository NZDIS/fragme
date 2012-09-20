package org.nzdis.fragme.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {

/**
* Returns the address of the current host, which satisfies name, protocol and scope.
*/
    public static boolean debugGetNonLoopbackAddress = false;
    public static final String IPV4 = "IPV4";
    public static final String IPV6 = "IPV6";
    public static final String SCOPE_LINK_LOCAL = "SCOPE_LINK_LOCAL";
    public static final String SCOPE_SITE_LOCAL = "SCOPE_SITE_LOCAL";
    public static final String SCOPE_GLOBAL = "SCOPE_GLOBAL";
    public static final String DEFAULT_INTERFACE_NAME = null;
    public static final String DEFAULT_PROTOCOL = IPV4;
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
				System.err.println("NetworkInterface " + interfaceName + " was not found");
				return null;
			}
			return (getNonLoopbackAddress(intface, protocol, scope));
			
		} else { // No interface name specified, so get all interfaces
			Enumeration<NetworkInterface> interfacesEnumerator = null;
			String address = null;
			try {
				interfacesEnumerator = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {
				System.err.println("A problem occurred when trying to obtain the list of available NetworkInterfaces");
			}
			while (interfacesEnumerator.hasMoreElements()) {
				// Check each interface
				intface = interfacesEnumerator.nextElement();
				address = getNonLoopbackAddress(intface, protocol, scope);
				if (address != null) {
					return address;
				}
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
		if (protocol.toUpperCase().equals("IPV4")) {
			protocolClass = Inet4Address.class;
		} else if (protocol.toUpperCase().equals("IPV6")) {
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

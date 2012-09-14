package org.nzdis.fragme.util;

public class DetermineOS {

	public static final String WINDOWS = "WINDOWS";
	public static final String LINUX = "LINUX";
	public static final String UNIX = "UNIX";
	public static final String MAC_OS = "MAC";
	public static final String ANDROID = "ANDROID";
	public static final String UNKNOWN = "UNKNOWN";
	
	public static String getOS(){
		String os = System.getProperty("os.name").toUpperCase();
		//System.out.println("OS: " + os);
		if(os.startsWith(WINDOWS)){
			return WINDOWS;
		} else if(os.startsWith(UNIX)){
			return UNIX;
		} else if(os.startsWith(MAC_OS)){
			return MAC_OS;
		} else if(os.startsWith(LINUX)){
			if(System.getProperty("java.vm.name").startsWith("Dalvik")){
				return ANDROID;
			}
			return LINUX;
		}
		return UNKNOWN;
	}

}

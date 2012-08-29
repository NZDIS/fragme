package org.nzdis.fragme.peers;

// moved from PeerManagerImpl 23/7/2008 
public class TypeWrappers {

	/**
	 * Nested top-level class FlagBool - a wrapper of a boolean variable
	 */
	public static class FlagBool {
		/**
		 * datafield to store the boolean value
		 */
		private boolean value;

		/**
		 * Constructor that sets the value datafield
		 * 
		 * @param value
		 *            the boolean value to be set
		 */
		public FlagBool(boolean value) {
			this.value = value;
		}

		/**
		 * Sets the boolean value
		 * 
		 * @param value
		 *            the boolean value
		 */
		public void setValue(boolean value) {
			this.value = value;
		}

		/**
		 * Returns the boolean value
		 * 
		 * @return the boolean value
		 */
		public boolean getValue() {
			return this.value;
		}
	}

	/**
	 * Nested top-level class FlagInt - a wrapper of a int variable
	 */
	public static class FlagInt {
		/**
		 * datafield to store the int value
		 */
		private int value;

		/**
		 * Constructor to set the value
		 * 
		 * @param value
		 *            the value of the FlagInt
		 */
		public FlagInt(int value) {
			this.value = value;
		}

		/**
		 * Sets the value of the FlagInt
		 * 
		 * @param value
		 *            the value of the FlagInt
		 */
		public void setValue(int value) {
			this.value = value;
		}

		/**
		 * Returns the value
		 * 
		 * @return value in int
		 */
		public int getValue() {
			return this.value;
		}
	}

}

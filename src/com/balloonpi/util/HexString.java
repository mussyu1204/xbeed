package com.balloonpi.util;

/**
 * Get hex String.
 * 
 * @author shintaro
 * @version 1.0
 * 
 */
public class HexString {
	/**
	 * Get hex string from input string.
	 * 
	 * @param in
	 * @return Hex String
	 */
	public String toHexString(String in) {
		StringBuffer out = new StringBuffer();

		byte[] bytes = in.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			out.append("0x");
			out.append(Integer.toHexString(bytes[i] & 0xff));
			out.append(",");
		}
		out.deleteCharAt(out.length() - 1);
		return out.toString();
	}

	/**
	 * Get hex string from Byte list.
	 * 
	 * @param in
	 * @return Hex String
	 */
	public String toHexString(Byte[] in) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < in.length; i++) {
			out.append("0x");
			out.append(Integer.toHexString(in[i] & 0xff));
			out.append(",");
		}
		out.deleteCharAt(out.length() - 1);
		return out.toString();
	}

	/**
	 * Get hex String from Byte.
	 * 
	 * @param in
	 * @return Hex String
	 */
	public String toHexString(byte in) {
		StringBuffer out = new StringBuffer();

		out.append("0x");
		out.append(Integer.toHexString(in & 0xff));

		return out.toString();
	}
}

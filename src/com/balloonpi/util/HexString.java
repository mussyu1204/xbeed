package com.balloonpi.util;

public class HexString {
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

	public String toHexString(byte in) {
		StringBuffer out = new StringBuffer();

		out.append("0x");
		out.append(Integer.toHexString(in & 0xff));

		return out.toString();
	}
}

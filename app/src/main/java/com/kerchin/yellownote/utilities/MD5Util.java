//package com.kerchin.yellownote.utilities;
//
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//public class MD5Util {
//	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
//			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
//	private static final String MD5_KEY = "2f9*p#omg";
//
//	public static String toHexString(byte[] b) {
//		StringBuilder sb = new StringBuilder(b.length * 2);
//		for (byte aB : b) {
//			sb.append(HEX_DIGITS[(aB & 0xf0) >>> 4]);
//			sb.append(HEX_DIGITS[aB & 0x0f]);
//		}
//		return sb.toString();
//	}
//
//	public static String md5Phone(final String phone) {
//		if (phone != null) {
//			return md5(phone + MD5_KEY);
//		}
//		return "";
//	}
//
//	public static byte[] md5Bytes(String text) throws Exception {
//		if (null == text || "".equals(text)) {
//			return new byte[0];
//		}
//
//		MessageDigest msgDigest;
//		try {
//			msgDigest = MessageDigest.getInstance("MD5");
//		} catch (NoSuchAlgorithmException e) {
//			throw new IllegalStateException(
//					"System doesn't support MD5 algorithm.");
//		}
//		byte[] bytes;
//		synchronized (msgDigest) {
//			msgDigest.update(text.getBytes());
//			bytes = msgDigest.digest();
//		}
//		return bytes;
//	}
//
//	public static String md5(String text, boolean isReturnRaw) {
//		if (null == text || "".equals(text)) {
//			return text;
//		}
//
//		byte[] bytes;
//		try {
//			bytes = md5Bytes(text);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "";
//		}
//		if (isReturnRaw) {
//			return new String(bytes);
//		}
//
//		String md5Str = "";
//		byte tb;
//		char low;
//		char high;
//		char tmpChar;
//
//		for (byte aByte : bytes) {
//			tb = aByte;
//
//			tmpChar = (char) ((tb >>> 4) & 0x000f);
//			if (tmpChar >= 10) {
//				high = (char) (('a' + tmpChar) - 10);
//			} else {
//				high = (char) ('0' + tmpChar);
//			}
//			md5Str += high;
//
//			tmpChar = (char) (tb & 0x000f);
//			if (tmpChar >= 10) {
//				low = (char) (('a' + tmpChar) - 10);
//			} else {
//				low = (char) ('0' + tmpChar);
//			}
//			md5Str += low;
//		}
//
//		return md5Str;
//	}
//
//	public static String md5(String text) {
//		return md5(text, false);
//	}
//
//	@SuppressWarnings("resource")
//	public static String md5Sum(String filename) {
//		InputStream fis;
//		byte[] buffer = new byte[1024];
//		int numRead;
//		MessageDigest md5;
//		try {
//			fis = new FileInputStream(filename);
//			md5 = MessageDigest.getInstance("MD5");
//			if (null == md5)
//				return "";
//			byte[] md5dist = null;
//			synchronized (md5) {
//				while ((numRead = fis.read(buffer)) > 0) {
//					md5.update(buffer, 0, numRead);
//				}
//				fis.close();
//				md5dist = md5.digest();
//			}
//			return toHexString(md5dist);
//		} catch (Exception e) {
//			System.out.println("error");
//			return null;
//		}
//	}
//}

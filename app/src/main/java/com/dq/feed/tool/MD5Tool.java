package com.dq.feed.tool;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Tool {

	public static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	    
	    /**
		 * MD5����
		 * 
		 * @param s
		 * @return
		 * @author kevin.xia
	     * @throws NoSuchAlgorithmException 
	     * @throws UnsupportedEncodingException 
		 */
		public final static String getMD5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
				MessageDigest mdTemp = MessageDigest.getInstance("MD5");
				mdTemp.update(s.getBytes("utf-8"));
				byte[] md = mdTemp.digest();

				char str[] = new char[md.length * 2];
				for (int i = 0, k = 0; i < md.length; i++) {
					str[k++] = hexDigits[md[i] >>> 4 & 0xf];
					str[k++] = hexDigits[md[i] & 0xf];
				}
				return new String(str).toLowerCase();

		}

	
}

package com.example.fertilizercrm.common.utils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES对称加密
 * @author tong
 *
 */
public class AESUtil {
	public static void main(String[] args) {  
		String content = "bocop";  
		String password = "12345678";  
		byte[] encryptResult = encode(content, password);//加密  
		byte[] decryptResult = decode(encryptResult,password);//解密  
		/*
		  这主要是因为加密后的byte数组是不能强制转换成字符串的, 换言之,字符串和byte数组在这种情况下不是互逆的, 
		  要避免这种情况，我们需要做一些修订，可以考虑将二进制数据转换成十六进制表示, 
		 主要有两个方法:将二进制转换成16进制(见方法parseByte2HexStr)或是将16进制转换为二进制(见方法parseHexStr2Byte)
		 */  
		String encryptResultStr = parseByte2HexStr(encryptResult);  
		System.out.println("加密后：" + encryptResultStr);  
		byte[] decryptFrom = parseHexStr2Byte(encryptResultStr);  
		decryptResult = decode(decryptFrom,password);//解码  
		System.out.println("解密后：" + new String(decryptResult));  
	}  

	/** 
	 * 加密 
	 *  
	 * @param content 需要加密的内容 
	 * @param password  加密密码 
	 * @return 
	 */  
	public static byte[] encode(String content, String password) {  
		try {             
			KeyGenerator kgen = KeyGenerator.getInstance("AES");  
			kgen.init(128, new SecureRandom(password.getBytes()));  
			SecretKey secretKey = kgen.generateKey();  
			byte[] enCodeFormat = secretKey.getEncoded();  
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");  
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器   
			byte[] byteContent = content.getBytes("utf-8");  
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化   
			byte[] result = cipher.doFinal(byteContent);  
			return result; // 加密   
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
		return null;  
	}  

	/**解密 
	 * @param content  待解密内容 
	 * @param password 解密密钥 
	 * @return 
	 */  
	public static byte[] decode(byte[] content, String password) {  
		try {  
			KeyGenerator kgen = KeyGenerator.getInstance("AES");  
			kgen.init(128, new SecureRandom(password.getBytes()));  
			SecretKey secretKey = kgen.generateKey();  
			byte[] enCodeFormat = secretKey.getEncoded();  
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");              
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器   
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化   
			byte[] result = cipher.doFinal(content);  
			return result; // 加密   
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
		return null;  
	}  


	/** 
	 * 将二进制转换成16进制 
	 * @method parseByte2HexStr 
	 * @param buf 
	 * @return 
	 * @throws  
	 * @since v1.0 
	 */  
	public static String parseByte2HexStr(byte buf[]){  
		StringBuffer sb = new StringBuffer();  
		for(int i = 0; i < buf.length; i++){  
			String hex = Integer.toHexString(buf[i] & 0xFF);  
			if (hex.length() == 1) {  
				hex = '0' + hex;  
			}  
			sb.append(hex.toUpperCase());  
		}  
		return sb.toString();  
	}  

	/** 
	 * 将16进制转换为二进制 
	 * @method parseHexStr2Byte 
	 * @param hexStr 
	 * @return 
	 * @throws  
	 * @since v1.0 
	 */  
	public static byte[] parseHexStr2Byte(String hexStr){  
		if(hexStr.length() < 1)  
			return null;  
		byte[] result = new byte[hexStr.length() / 2];  
		for (int i = 0;i< hexStr.length()/2; i++) {  
			int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
			int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
			result[i] = (byte) (high * 16 + low);  
		}  
		return result;  
	}  
}

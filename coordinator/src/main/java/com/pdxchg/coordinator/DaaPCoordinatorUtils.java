package com.pdxchg.coordinator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.spongycastle.util.encoders.Hex;

import com.pdxchg.ethereum.crypto.ECKey;
import com.pdxchg.ethereum.crypto.HashUtil;
import com.pdxchg.ethereum.crypto.ECKey.ECDSASignature;
import com.pdxchg.ethereum.util.ByteUtil;

//import com.pdxchg.ethereum.crypto.SHA3Helper;

public class DaaPCoordinatorUtils {
	
	public static final String SESSION_TIMESTAMP_MAP = "daapCoordinator-session-timestamp";
	public static final String EVENT_TOKEN_SCOPE = "pdx-user-events";
	public static byte DAAP_JOIN_MESSAGE = 0x01;
	public static byte DAAP_HB_MESSAGE = 0x02;
	private static String key = "SqGSIb3DQEBAQUAA";
	
	public static String getToken(String containerIP, String coordinatorIP, long  expireTime,  
			int  random, byte[] salt ) 
			throws Exception {
		
		byte[] containerIPBtyes = intToByte(ipToInt(containerIP));
		byte[] coordinatorIPBytes = intToByte(ipToInt(coordinatorIP));
		byte[] expireBytes = longToByte(expireTime);
		byte[] randBytes = intToByte(random);
		
		byte[] allBytes = new byte[4 + 4 + 8 + 4 + salt.length]; 
		System.arraycopy(containerIPBtyes, 0, allBytes, 0, containerIPBtyes.length);
		System.arraycopy(coordinatorIPBytes, 0, allBytes, containerIPBtyes.length, coordinatorIPBytes.length);
		System.arraycopy(expireBytes, 0, allBytes, containerIPBtyes.length+coordinatorIPBytes.length, expireBytes.length);
		System.arraycopy(randBytes, 0, allBytes, containerIPBtyes.length+coordinatorIPBytes.length + expireBytes.length, randBytes.length);
		System.arraycopy(salt, 0, allBytes, 
				containerIPBtyes.length+coordinatorIPBytes.length + 
				expireBytes.length+ randBytes.length, salt.length);
		return Encrypt(allBytes, key);	
	  }

	  public static long getExpireTime(String token) throws Exception {
		  byte[] allBytes = Decrypt(token, key);
		  byte[] expireBytes = new byte[8];
		  System.arraycopy(allBytes, 8, expireBytes, 0, 8);
		  return byteToLong(expireBytes);
	  }
	  
	  public static String getIP(String token) {
		  
		try {
			byte[] allBytes = Decrypt(token,  key);
		    byte[] ipBytes = new byte[4];
		  System.arraycopy(allBytes, 0, ipBytes, 0, 4); 
		  return bytesToIp(ipBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return  null;
		}	
	  }
	  
	  
	   //AES cipher
	   public static String Encrypt(byte[] sSrc, String sKey) throws Exception {
		   
		    String ret = null;
	        if (sKey.length() == 16) {
	        
	        	byte[] raw = sKey.getBytes("utf-8");
	        	SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	        	Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
	        	cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	        	byte[] encrypted = cipher.doFinal(sSrc);
	 
	        ret =  Base64.getEncoder().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
	        }  
	        return  ret;
	   }
	 
	    // AES Decipher
	    public static byte[] Decrypt(String sSrc, String sKey) throws Exception {
	           
	        	byte[] original = null;
	        	if (sKey.length() == 16 ) {
	        		byte[] raw = sKey.getBytes("utf-8");
	        		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	        		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	        		byte[] encrypted1 = Base64.getDecoder().decode(sSrc);//先用base64解密
	        		try {
	        			original = cipher.doFinal(encrypted1);
	        		} catch (Exception e) {
	        			System.out.println(e.toString());
	        		}
	        	}
	        	return original;
	    }
	    
	  public static String hexToString(byte[] b) {
		    String  hex="";
	        for (int i = 0; i < b.length; i++)
	        {
	            hex = Integer.toHexString(b[i] & 0xFF);
	            if (hex.length() == 1)
	            {
	                hex = '0' + hex;
	            }
	        }
	        return hex;
	  }
	  
	  public static void printHexString(byte[] b)
	    {
	        for (int i = 0; i < b.length; i++)
	        {
	            String hex = Integer.toHexString(b[i] & 0xFF);
	            if (hex.length() == 1)
	            {
	                hex = '0' + hex;
	            }
	            System.out.print(hex.toUpperCase() + " ");
	        }
	        System.out.println("");
	    }
	  
	  	public static long byteToLong(byte[] byteNum) {  
	  	    long s = 0; 
	        long s0 = byteNum[0] & 0xff;  
	        long s1 = byteNum[1] & 0xff; 
	        long s2 = byteNum[2] & 0xff; 
	        long s3 = byteNum[3] & 0xff; 
	        long s4 = byteNum[4] & 0xff; 
	        long s5 = byteNum[5] & 0xff; 
	        long s6 = byteNum[6] & 0xff; 
	        long s7 = byteNum[7] & 0xff; 
	 
	        // s0不变 
	        s1 <<= 8; 
	        s2 <<= 16; 
	        s3 <<= 24; 
	        s4 <<= 8 * 4; 
	        s5 <<= 8 * 5; 
	        s6 <<= 8 * 6; 
	        s7 <<= 8 * 7; 
	        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7; 
	        return s;  
	  	}
	  	
	   public static byte[] longToByte(long number) { 
	        long temp = number; 
	        byte[] b = new byte[8]; 
	        for (int i = 0; i < b.length; i++) { 
	            b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位 
	            temp = temp >> 8; // 向右移8位 
	        } 
	        return b; 
	    } 
	   
	   public static byte[] intToByte(int number) {
		   byte[] b = new byte[4];
		   b[0] = (byte) (number & 0xff);// 最低位   
		   b[1] = (byte) ((number >> 8) & 0xff);// 次低位   
		   b[2] = (byte) ((number >> 16) & 0xff);// 次高位   
		   b[3] = (byte) (number >>> 24);// 最高位,无符号右移。
		   return b;
	   }
	   
	    public static byte[] ipToBytes(String ipAddr) {
	        byte[] ret = new byte[4];
	        try {
	            String[] ipArr = ipAddr.split("\\.");
	            ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
	            ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
	            ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
	            ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
	            return ret;
	        } catch (Exception e) {
	            throw new IllegalArgumentException(ipAddr + " is invalid IP");
	        }
	    }

	    public static int bytesToInt(byte[] bytes) {
	    	int addr = bytes[0] & 0xFF;
	    	addr |= ((bytes[1] << 8) & 0xFF00);
	    	addr |= ((bytes[2] << 16) & 0xFF0000);
	    	addr |= ((bytes[3] << 24) & 0xFF000000);
	    	return addr;
	    }
	    
	    public static int ipToInt(String ipAddr) {
	    	try {
	    		return bytesToInt(ipToBytes(ipAddr));
	    	} catch (Exception e) {
	    		throw new IllegalArgumentException(ipAddr + " is invalid IP");
	    	}
	    }
	    
	    public static String bytesToIp(byte[] bytes) {
	        return new StringBuffer().append(bytes[0] & 0xFF).append('.').append(
	                bytes[1] & 0xFF).append('.').append(bytes[2] & 0xFF)
	                .append('.').append(bytes[3] & 0xFF).toString();
	    }

	   public static String  formatIP(String str)  {
		   String realIP = null;
		   String[] ipIDs = str.split("/");
		   if (ipIDs.length == 2) {
			   String ipID = ipIDs[1];
			   String[] ipaddrs = ipID.split(":");
			   if (ipaddrs.length >= 2) {
				   realIP = ipaddrs[0];
			   }
		   }
		   return realIP;
	   }
	   
	   public static PublicKey getPubKey(String  encodedPubKey) {
			  PublicKey publicKey = null;
			  try {

			   
			    //String pubKey ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVRiDkEKXy/KBTe+UmkA+feq1zGWIgBxkgbz7aBJGb5+eMKKoiDRoEHzlGndwFKm4mQWNftuMOfNcogzYpGKSEfC7sqfBPDHsGPZixMWzL3J10zkMTWo6MDIXKKqMG1Pgeq1wENfJjcYSU/enYSZkg3rFTOaBSFId+rrPjPo7Y4wIDAQAB";
			      java.security.spec.X509EncodedKeySpec  pubKeySpec = new java.security.spec.X509EncodedKeySpec(
			    		  Base64.getDecoder().decode(encodedPubKey));
			      
			   java.security.KeyFactory keyFactory;
			   keyFactory = java.security.KeyFactory.getInstance("RSA");
			   publicKey = keyFactory.generatePublic(pubKeySpec);
			  } catch (NoSuchAlgorithmException e) {
			   e.printStackTrace();
			  } catch (InvalidKeySpecException e) {
			   e.printStackTrace();
			  }
			  return publicKey;
			 }
		
	   
		 public static boolean verifySignature(int random, long timeStamp, byte[] salt, byte[]signatureBytes, String publicKey)
		           throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
						 
				
				int randomSize = Integer.toString(random).getBytes().length;
				int timeStampSize = Long.toString(timeStamp).getBytes().length;
				
				byte[] data = new byte[randomSize + timeStampSize + salt.length];
				
				System.arraycopy(Integer.toString(random).getBytes(), 0, data, 0, randomSize);
				System.arraycopy(Long.toString(timeStamp).getBytes(), 0, data, randomSize, timeStampSize);
				System.arraycopy(salt, 0, data, randomSize + timeStampSize, salt.length);

				byte[] hash  = HashUtil.sha3(data);
				byte[] r = new byte[32];
				System.arraycopy(signatureBytes, 1, r, 0, 32);
				byte[] s = new byte[32];
				System.arraycopy(signatureBytes, 33, s, 0, 32);
				ECDSASignature ecSignature = new ECDSASignature(ByteUtil.bytesToBigInteger(r), ByteUtil.bytesToBigInteger(s));
				return ECKey.verify(hash, ecSignature, Hex.decode(publicKey));	
		   }
		 
		 
}

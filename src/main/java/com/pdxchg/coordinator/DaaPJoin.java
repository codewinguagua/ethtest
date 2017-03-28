package com.pdxchg.coordinator;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import com.google.gson.Gson;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;



public class DaaPJoin {
	
	private int random;
	private long timeStamp;
	private byte[] salt;
	private byte[] signature;
	private String nodePubKey;
	
	
	public DaaPJoin(int rand, long timestamp, byte[] salt,  byte[] signature, String pubKey) {
		this.random =  rand;
		this.timeStamp = timestamp;
		this.salt = salt;
		this.signature = signature;
		this.nodePubKey = pubKey;
	}
	
	public DaaPJoin() {
		
	}
	public String getNodePubKey() {
		return this.nodePubKey;
	}
	
	public void  setRandom(int rand) {
		this.random = rand;
	}
	
	public  void setTimeStamp(long timeStamp) {
		this.timeStamp =  timeStamp;
	}
	
	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
	
	public  void setSignature(byte[] sig) {
		this.signature = sig;
	}
	
	public int  getRandom() {
		return this.random;
	}
	
	public long getTimeStamp() {
		return this.timeStamp;
	}
	public byte[] getSalt() {
		return this.salt;
	}
	public byte[] getSignature() {
		return  this.signature;
	}
	
	public boolean verifySig() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		
		boolean ret = true;
		
		boolean isVerified = ((String)ContextListener.getInstance().getProps().get("coordinator.verifySig")).equals("true")? true : false;

		System.out.println("isVerfied  = " + isVerified + "           " +  ContextListener.getInstance().getProps().get("coordinator.verifySig"));
		if ( isVerified ) {
			ret = DaaPCoordinatorUtils.verifySignature(this.random, this.timeStamp, this.salt, this.signature, this.nodePubKey);
		}
		return ret;
	}
	
	public static class joinMessageEncoder implements Encoder.Binary<DaaPJoin> {
	
		public void init( final EndpointConfig config ) {
		}
		
		public ByteBuffer encode(  DaaPJoin message ) throws EncodeException {
			
			Gson gson = new Gson();
			String str = gson.toJson(message);
			ByteBuffer buffer = ByteBuffer.allocate(str.getBytes().length + 1);
			
			//initialization request type
			buffer.put((byte) DaaPCoordinatorUtils.DAAP_JOIN_MESSAGE);  
			try {
				buffer.put(str.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer.position(0);
			return buffer;
			//return  ByteBuffer.wrap(str.getBytes());
		}
		

		public void destroy() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
    public static class joinMessageDecoder implements Decoder.Binary<DaaPJoin> {
		
		public void init( final EndpointConfig config ) {
		}  
		
		public DaaPJoin decode( ByteBuffer receving) throws DecodeException   {
			
			DaaPJoin message = new DaaPJoin();
			try {
			receving.get();
	        Charset charset = Charset.forName("UTF-8");
	        CharsetDecoder decoder = charset.newDecoder();
	        CharBuffer charBuffer = decoder.decode(receving.asReadOnlyBuffer());
	        String str = charBuffer.toString();
			Gson gson = new Gson();
	        message = gson.fromJson(str, DaaPJoin.class);
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			}
			return message;
		}
		
		public boolean willDecode(ByteBuffer receving ) {
			return true;
		}
		
		public void destroy() {
		}
	    }

}	

package com.pdxchg.coordinator;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Calendar;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import com.google.gson.Gson;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;

public class DaaPHB {
	private long timeStamp;
	private String token;
	private ArrayList<String> bundlePool;
	
	public DaaPHB() {
	}
	
	public DaaPHB(String token, long timeStamp, ArrayList<String> pool ){
		this.timeStamp = timeStamp;
		this.token = token;
		this.bundlePool = pool;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public long  getTimeStamp() {
		return this.timeStamp;
	}
	
	public ArrayList<String> getPool() {
		return this.bundlePool;
	}
	
	public void  setToken(String token) {
		this.token =  token;
	}
	
	public void setTimeStamp(long time) {
		this.timeStamp = time;
	}
	
	public void setPool(ArrayList<String> pool) {
		this.bundlePool = pool;
	}
	
	public boolean isTokenExpired() throws Exception {
		long expireTime = DaaPCoordinatorUtils.getExpireTime(token);
		
		Calendar now = Calendar.getInstance();
		long currentTime = now.getTimeInMillis();
		if (expireTime > currentTime) return false;
			else  return true;
	}
	
	public static class hbMessageEncoder implements Encoder.Binary<DaaPHB> {
		
		public void init( final EndpointConfig config ) {
		}
		
		public ByteBuffer encode(  DaaPHB message ) throws EncodeException {
			
			Gson gson = new Gson();
			String str = gson.toJson(message);
			
			ByteBuffer buffer = ByteBuffer.allocate(str.getBytes().length + 1);
			
			//heart beat request
			buffer.put((byte) DaaPCoordinatorUtils.DAAP_HB_MESSAGE); 
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
	
    public static class hbMessageDecoder implements Decoder.Binary<DaaPHB> {
		
		public void init( final EndpointConfig config ) {
		}  
		
		public DaaPHB decode( ByteBuffer receving) throws DecodeException   {
			
			DaaPHB message = new DaaPHB();
			try {
			receving.get();
	        Charset charset = Charset.forName("UTF-8");
	        CharsetDecoder decoder = charset.newDecoder();
	        CharBuffer charBuffer = decoder.decode(receving.asReadOnlyBuffer());
	        String str = charBuffer.toString();
			Gson gson = new Gson();
	        message = gson.fromJson(str, DaaPHB.class);
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

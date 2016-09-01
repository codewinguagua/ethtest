package com.pdxchg.coordinator;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

import com.pdxchg.coordinator.DaaPRange;

public class DaaPRange {
	
	private int nodeSize;
	private String  token;
	ArrayList<String> bundlePool;
	
	public  DaaPRange() {		
	}
	
	public DaaPRange(String token, ArrayList<String> pool, int size) {
			this.token = token;
			this.bundlePool =  pool;
			this.nodeSize  = size;
	}
	
	public void  setToken(String  token) {
		this.token =  token;
	}
	
	public void setPool(ArrayList<String> pool) {
		this.bundlePool = pool;
	}
	
	public String getToken() {
		return  this.token;
	}
	
	public void setNodeSize(int size) {
		this.nodeSize = size;
	}
	
	public  int getNodeSize() {
		return this.nodeSize;
	}
	
	public ArrayList<String> getPool() {
		return this.bundlePool;
	}
	
	public static class rangeMessageEncoder implements Encoder.Binary<DaaPRange> {
		
		public void init( final EndpointConfig config ) {
		}
		
		public ByteBuffer encode(  DaaPRange message ) throws EncodeException {
			Gson gson = new Gson();
			String str = gson.toJson(message);
			return ByteBuffer.wrap(str.getBytes()) ;
		}
		

		public void destroy() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
    public static class rangeMessageDecoder implements Decoder.Binary< DaaPRange > {
		
		public void init( final EndpointConfig config ) {
		}
		
		public DaaPRange decode( ByteBuffer receving ) throws DecodeException {
			DaaPRange message = new DaaPRange();
			try {
	        Charset charset = Charset.forName("UTF-8");
	        CharsetDecoder decoder = charset.newDecoder();
	        CharBuffer charBuffer = decoder.decode(receving.asReadOnlyBuffer());
	        String str = charBuffer.toString();
			Gson gson = new Gson();
	        message = gson.fromJson(str, DaaPRange.class);
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			}
			return message;
		}
		
		public boolean willDecode( final ByteBuffer receiving ) {
			return true;
		}
		
		public void destroy() {
		}
	    }
}

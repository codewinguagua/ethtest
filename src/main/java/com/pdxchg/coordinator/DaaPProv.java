package com.pdxchg.coordinator;

import java.io.UnsupportedEncodingException;
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

public class DaaPProv {
	
	private ArrayList<String> bundles;
	private String provType;
	
	
	public  DaaPProv(ArrayList<String> bundles, String provType) {
		this.bundles = bundles;
		this.provType = provType;
	}
	public DaaPProv() {
		
	}

	public void setBundles(ArrayList<String> bundles) {
		this.bundles = bundles;
	}

	public void setProvType(String provType) {
		this.provType  = provType;
	}
	
	public String getProvType() {
		return this.provType;
	}
	
   public ArrayList<String> getBundles() {
	   return this.bundles;
   }
	
	public static class provMessageEncoder implements Encoder.Text<DaaPProv> {
	
		public void init( final EndpointConfig config ) {
		}
		
		public String  encode(  DaaPProv message ) throws EncodeException {
			
			Gson gson = new Gson();
			return  gson.toJson(message);
			//return  ByteBuffer.wrap(str.getBytes());
		}
		

		public void destroy() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
    public static class provMessageDecoder implements Decoder.Text<DaaPProv> {
		
		public void init( final EndpointConfig config ) {
		}  
		
		public DaaPProv decode( String receving) throws DecodeException   {
			
			DaaPProv message = new DaaPProv();
			Gson gson = new Gson();
		    message = gson.fromJson(receving, DaaPProv.class);
			return message;
		}
		
		public boolean willDecode(String receving ) {
			return true;
		}
		
		public void destroy() {
		}
	    }
  
}

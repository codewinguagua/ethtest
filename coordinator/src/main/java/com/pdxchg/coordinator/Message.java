package com.pdxchg.coordinator;

import javax.websocket.Decoder;

import java.io.StringReader;
import java.util.Collections;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

import com.pdxchg.coordinator.Message;


public class Message {
	
	public static class MessageEncoder implements Encoder.Text< Message > {
		
		public void init( final EndpointConfig config ) {
		}
		
		public String encode( final Message message ) throws EncodeException {
			Gson gson = new Gson();
	        String str = gson.toJson(message);
			return str;
		}

		public void destroy() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class MessageDecoder implements Decoder.Text< Message > {
		
		public void init( final EndpointConfig config ) {
		}
		
		public Message decode( String str ) throws DecodeException {
			final Message message = new Message();
			 Gson gson = new Gson();
	          Message pojo = gson.fromJson(str, Message.class);
			  message.setMessage(pojo.getMessage());
			  message.setUsername(pojo.getUsername());
			return message;

		}
		
		public boolean willDecode( final String str ) {
			return true;
		}
		
		public void destroy() {
		}
	    }
	
	    private String username;
	    private String message;

	    public Message() {
	    }

	    public Message( final String username, final String message ) {
	        this.username = username;
	        this.message = message;
	    }

	    public String getMessage() {
	        return message;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public void setMessage( final String message ) {
	        this.message = message;
	    }

	    public void setUsername( final String username ) {
	        this.username = username;
	    }
	    
}

package com.pdxchg.coordinator;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang.StringUtils;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;
import com.pdxchg.coordinator.DaaPRange.rangeMessageDecoder;
import com.pdxchg.coordinator.DaaPRange.rangeMessageEncoder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@ServerEndpoint(
	    value = "/broadcast",
	    encoders = {  rangeMessageEncoder.class },
	    decoders = {  rangeMessageDecoder.class }
)  

public class CoordinatorEndpoint {
	
	private Sessions mySessions = Sessions.getInstance();
	private static final String REMOTE_ADDRESS="javax.websocket.endpoint.remoteAddress";
	private String token =  null;
	private Long timeStamp;
	
	private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);


    @OnOpen
    public void onOpen( final Session client ) {
    	logger.info("Connection request from  remote endpoint");
    	
    }

    @OnClose
    public void onClose( final Session client, CloseReason closeReason ) {
    	logger.debug("Receving  closing message of " + token);
    	if (token != null && closeReason.getReasonPhrase().equals("Shutdown") == true) {     
    		Rebalancing  processing = new Rebalancing(token, client, timeStamp);
    		System.out.println("Endpoint shutdown!");
    		try {
    			processing.rebalanceByNodeRemoval(token);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	// mySessions.remove(token);    
    }

    
    @OnMessage
    public void onMessage(  ByteBuffer message,  Session client )
            throws Exception  {
    	
    		byte Tag = message.get();
        	Charset charset = Charset.forName("UTF-8");
        	CharsetDecoder decoder = charset.newDecoder();
        	CharBuffer charBuffer = decoder.decode(message.asReadOnlyBuffer());
        	String str = charBuffer.toString();
        
        	if (Tag == DaaPCoordinatorUtils.DAAP_JOIN_MESSAGE) {
        		
        		//connection request
        	
        		logger.debug("Join Message");
    			DaaPJoin joinReq = new DaaPJoin();
    			Gson gson = new Gson();
    	        joinReq = gson.fromJson(str, DaaPJoin.class);
    	        
    	        if (joinReq.verifySig() == true ) {
    	        		timeStamp = joinReq.getTimeStamp();
    	        		long expireTime = timeStamp + 600000;
    	        		System.out.println(client.getUserProperties().get(REMOTE_ADDRESS));
    	        		System.out.println("Server IP = " + InetAddress.getLocalHost().getHostAddress().toString());
    	        		String containerIP = DaaPCoordinatorUtils.formatIP(
    	        				(String)client.getUserProperties().get(REMOTE_ADDRESS).toString());
    
    	        		token = DaaPCoordinatorUtils.getToken( containerIP, 
    	        				InetAddress.getLocalHost().getHostAddress().toString(),
    	        				expireTime, joinReq.getRandom(), joinReq.getSalt());
    	 
   	        		
    	        		logger.debug(" token = " + token +  " | random =  " +  joinReq.getRandom() + " | timeStamp  = " 
    	        				 + joinReq.getTimeStamp() 
    	        				 + " | Salt = " +  DaaPCoordinatorUtils.hexToString(joinReq.getSalt())
    	        				 +  " | Signature = " +  DaaPCoordinatorUtils.hexToString(joinReq.getSignature())
    	        				 + " | Rmote IP = " + InetAddress.getLocalHost().getHostAddress().toString());

    	        			Rebalancing processing = new Rebalancing(token, client, timeStamp);
    	        			processing.rebalanceByNewNode(token, containerIP, joinReq.getNodePubKey());
   
    	        		
    	        } else {
    	        	client.getBasicRemote().sendText("Verification failed");
    	        	logger.warn("Signature  verfied failed");
    	        }
    	    	
        	} else if ( Tag == DaaPCoordinatorUtils.DAAP_HB_MESSAGE){
        		//heart beat  request
        		
        		DaaPHB hbReq = new DaaPHB();
        		Gson gson= new Gson();       		
        		hbReq = gson.fromJson(str,DaaPHB.class);
        		
        		logger.debug("Heart beat message: token = " + hbReq.getToken() +  "| timeStamp = "
        					 + hbReq.getTimeStamp() + "| Pool =  "  + hbReq.getPool().toString());
        		
        		//if (mySessions.verifyToken(hbReq.getToken()) == true && hbReq.isTokenExpired() == false) {
        		if (mySessions.verifyToken(hbReq.getToken()) == true ) {
        			SessionTimeStamp latestStatus  = new SessionTimeStamp(hbReq.getToken(), hbReq.getTimeStamp());
        			latestStatus.updateStatus();        			
        		
        		} else {
        			client.getBasicRemote().sendText("Token:  " + hbReq.getToken() + "is not verified");
        			logger.warn("Token:%s verified failed", hbReq.getToken());
        		}
        		     		
        	}
        	
       
    }
    
   /* @OnError 
    public void onError( final Session client ) {
    	try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }*/

}
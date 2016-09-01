package com.pdxchg.coordinator;

import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang.StringUtils;

import com.pdxchg.coordinator.DaaPProv.provMessageDecoder;
import com.pdxchg.coordinator.DaaPProv.provMessageEncoder;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;

	@ServerEndpoint(
		    value = "/provision/{access_token}",
		    encoders = {  provMessageEncoder.class },
		    decoders = {  provMessageDecoder.class }
	)  

	public class ProvisionEndpoint {
		
		
		private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);


	    @OnOpen
	    public void onOpen( @PathParam("access_token") String accessToken, final Session client ) throws IOException {
	    	System.out.println("Connection provision request from  remote endpoint = " + accessToken);
	    	
	    	
	    	String  user = Authenticator.authn(accessToken, DaaPCoordinatorUtils.EVENT_TOKEN_SCOPE);
	    	//System.out.println("AccessToken = "  + accessToken);
	    	
	    	if(StringUtils.isBlank(user)){
	        	client.close();   
	        	System.out.println("Access token verification failed");
	        	logger.debug("Access token can  not be verified, session closed");
	    	} 
	    	
	    }

	    @OnClose
	    public void onClose( final Session client ) {
	
	    }
	    
	     @OnMessage
	     public void onMessage( final DaaPProv provReq, final Session client ) throws IOException, EncodeException {
	             
	      
	             logger.debug("provision message -- " + provReq.getProvType() +  "  " + provReq.getBundles().toString());
	      	     Rebalancing processing = new Rebalancing();
	      	     	 
	             if  (provReq.getProvType().equals("ADD") ==  true) {
	            	 //provision
	            	 ArrayList<String> newAdded = new ArrayList<String>();
	            		for (String theBundle:provReq.getBundles()) {
	            			if (ContextListener.getInstance().getBundleList().contains(theBundle) == false) {
	            				newAdded.add(theBundle);
	            			}
	            		}
	            		processing.rebalanceByNewBundle(newAdded);
	            	 
	             } else if  (provReq.getProvType().equals("REMOVE") == true ) {
	            	    //de-provision
	            	 	
	            	 	for (String theBundle:provReq.getBundles()) {
	            	 		processing.rebalanceBySingleBundleRemoval(theBundle);
	            	 		logger.info("Total Bundles = " + ContextListener.getInstance().getBundleList().toString()); 
	            	 	}
	             }
	                   
	        }
}

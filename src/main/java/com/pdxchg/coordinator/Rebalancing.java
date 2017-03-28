package com.pdxchg.coordinator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;
import com.pdxchg.coordinator.NodeInfo;


public class Rebalancing {
	
	private NodeInfo theNode;
	private SessionTimeStamp  eventTime;
	private Sessions theSessionClient;
	private ArrayList<String>  existingBundles;
	
	private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);
	
	public Rebalancing(String token, Session client, long timeStamp) {
		theNode = new NodeInfo(token, client);
		eventTime = new SessionTimeStamp(token, timeStamp);
		this.theSessionClient = Sessions.getInstance();	
		this.existingBundles = ContextListener.getInstance().getBundleList();
	}
	
	
	public Rebalancing() {
		theNode = new NodeInfo();
		eventTime = new SessionTimeStamp();
		this.theSessionClient = Sessions.getInstance();	
		this.existingBundles = ContextListener.getInstance().getBundleList();	
	}
	    
	//REBALANCE  BY NEW NODE 
	public HashMap<String, ArrayList<String>> rebalanceByNewNode(String token, String containerIP, String pubKey )  {

		 synchronized(this) {
			 
			 LinkedHashMap<String, NodeInfo> indexMap = theSessionClient.getSessionTable();
			 HashMap<String, ArrayList<String>> returnValue = new HashMap<String, ArrayList<String>>();
			 int  nodes = indexMap.size();
			 int bundles = existingBundles.size();
		 
			 if  (indexMap.containsKey(token)) {
				 //duplicated node
				 try {
					theNode.getClient().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 } else {
			 if  (nodes == 0) {
				  theNode.setBundles(existingBundles);
				  theSessionClient.add(theNode.getToken(), theNode);
			 } else {
				 String  lastToken  = theSessionClient.isAlreadyIn(containerIP);
				 if (lastToken != null) {
					 logger.debug("Last token  found, relpace with new one " + lastToken + " with " + token + " for ip = " + containerIP);
					 theNode.setBundles(theSessionClient.getSessionTable().get(lastToken).getBundles());
					 try {
						theSessionClient.getSessionTable().get(lastToken).getClient().close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					theSessionClient.remove(lastToken);
					eventTime.remove(lastToken); 
				 } else {
				 ArrayList<String> newNodeBundles =  new ArrayList<String>();
				 if  (  bundles > nodes ) {
					 int nodeToBeSize = (int) Math.ceil((double)bundles / (nodes + 1));
					 int strongNodes = bundles % (nodes + 1);
					 int nodeID = 0;

					 Iterator<String> iter = indexMap.keySet().iterator();
					 while (iter.hasNext()) {
						 String key = iter.next();
						 ArrayList<String> bundle =  indexMap.get(key).getBundles();
						 Session nodeSession = indexMap.get(key).getClient();
						 int beginPos = bundle.size();
						 for (int cursor = beginPos; cursor > nodeToBeSize; cursor--) {
							 newNodeBundles.add(bundle.get(cursor-1));
							 bundle.remove(cursor-1);
						 }
						 //update the table accordingly
						 //NodeInfo node = new NodeInfo(key, nodeSession);
						 //node.setBundles(bundle);
						 //theSessionClient.replace(key, node);
						 returnValue.put(key, bundle);
						 if ( ++nodeID == strongNodes) nodeToBeSize--;
			        }
				 theNode.setBundles(newNodeBundles);
				 }	
			 }
			 }

			 theSessionClient.add(theNode.getToken(), theNode);
			 returnValue.put(theNode.getToken(), theNode.getBundles());
			 eventTime.add();
			 NodePubKey  nodepkey = new  NodePubKey(containerIP, pubKey, eventTime.getTimeStamp());
			 nodepkey.add();
			 NotifyAll();
			 }
	
		 	 return returnValue;
		   }
		}
	
    //REBALANCE BY  REMOVE  NODE
	public  HashMap<String,ArrayList<String>> rebalanceByNodeRemoval(String token)  {
		
		synchronized(this) {
			
			
			LinkedHashMap<String, NodeInfo> indexMap = theSessionClient.getSessionTable();
			HashMap<String, ArrayList<String>> returnValue = new HashMap<String, ArrayList<String>>();	
			
			if (indexMap.containsKey(token))  {
				//check the  avaliablity  of the token
	
			ArrayList<String> toBeReleasedBundles = (ArrayList<String>) indexMap.get(token).getBundles();
			
			int nodes = indexMap.size();
			int bundles  = existingBundles.size();
			if ( nodes > 1 && toBeReleasedBundles !=  null && toBeReleasedBundles.size() > 0) { 
				
			//if size is 0, remove the node does not matter, no  need to rebalance
				int  releaseSize = toBeReleasedBundles.size();
				int nodeToSize = (int)  Math.ceil((double)bundles/(nodes-1));
				int strongNodes = bundles % (nodes - 1);
				int nodeID = 0;
				Iterator<String> iter =  indexMap.keySet().iterator();
				//System.out.println("released size = " + releaseSize + " released bundle = " + toBeReleasedBundles.toString());
				int serialNo = 0;
				while (iter.hasNext()) {
					String key = iter.next();
					Session  nodeSession = indexMap.get(key).getClient();
					if (key.equals(token) == false) {
						ArrayList<String> bundle = (ArrayList<String>) indexMap.get(key).getBundles();
						int curPos = bundle.size();
					//System.out.println("curPos = " + curPos + " nodeToSize = " + nodeToSize + " StrongNodes = " + strongNodes + " serialNo = " + serialNo);
					for (int cursor = curPos; cursor<nodeToSize; cursor++) {
						bundle.add(toBeReleasedBundles.get(serialNo));
						serialNo++;
					}
					
					//NodeInfo node = new NodeInfo(key, nodeSession);
					//node.setBundles(bundle);
					//theSessionClient.replace(key, node);
					returnValue.put(key, bundle);
					//theSessionClient.replace(key, bundle);
					if ( ++nodeID == strongNodes) nodeToSize--;
				}  //skip the  node which would be removed 
				if ( serialNo == releaseSize ) break;	
				}	
			}
			theSessionClient.remove(theNode.getToken());
			eventTime.remove();
			NotifyAll();
			}
			return returnValue;
		  }
	  }
	
	//REBALANCE BY NEW BUNDLE
	public HashMap<String,  ArrayList<String>>  rebalanceByNewBundle(ArrayList<String> newBundles) throws IOException, EncodeException {
		
		synchronized(this) {
			
       	 	ArrayList<String> newAdded = new ArrayList<String>();
       	 	
       	 		for (String theBundle:newBundles) {
       	 			if (ContextListener.getInstance().getBundleList().contains(theBundle) == false) {
       	 				newAdded.add(theBundle);
       	 			}
       	 		}
       	 		
    
       	 	logger.debug(" New Added  Bundles = " + newAdded.toString());
       	 	ContextListener.getInstance().addBundles(newAdded);
 
       	 	logger.info("Total Bundles = " + ContextListener.getInstance().getBundleList().toString()); 
       	 		
			LinkedHashMap<String, NodeInfo> indexMap = theSessionClient.getSessionTable();
			HashMap<String, ArrayList<String>> returnValue = new HashMap<String, ArrayList<String>>();	 
			int nodes = indexMap.size();
			int totalBundleSize = existingBundles.size();
			int newSize = newAdded.size();
		  
			if ( nodes !=  0) {
				
					  int nodeToBeSize = (int) Math.ceil((double)totalBundleSize / nodes);
					  Iterator<String> iter =  indexMap.keySet().iterator();
					  int strongNodes = totalBundleSize % (nodes);
					  //System.out.println("nodeToBeSize = " + nodeToBeSize + "StrongNodes = " + strongNodes);
					  int serialNo = 0, nodeID = 0;
					  while (iter.hasNext()) {
						String key = iter.next();
						 ArrayList<String>bundle = indexMap.get(key).getBundles();
						Session nodeSession = indexMap.get(key).getClient();
							int cursorPos = bundle.size();
							for (int cursor = cursorPos; cursor<nodeToBeSize; cursor++) {
								bundle.add(newAdded.get(serialNo));
								serialNo++;
							}
							//NodeInfo node = new NodeInfo(key, nodeSession);
							//node.setBundles(bundle);
							//theSessionClient.replace(key, node);
							returnValue.put(key, bundle);
							if ( ++nodeID == strongNodes) nodeToBeSize--;
						if ( serialNo == newSize ) break;
					  }
			  	}
				NotifyAll();
			  return returnValue;
			}
	  	}
	
	
	
	//REBANLANCE BY  NODES REMOVAL
		public  int  notificationByNodesRemoval(ArrayList<String> tokens) throws IOException, EncodeException {
			
			synchronized(this) {
			
				LinkedHashMap<String, NodeInfo> indexMap = theSessionClient.getSessionTable();
				int num = 0;
				ArrayList<String> notificationNodes = new ArrayList<String>();
				
				for (String theToken:tokens) {
					if (indexMap.containsKey(theToken)) {  //check the availability  of key because different thread 
						indexMap.get(theToken).getClient().close();	
						num++;
					}
				}
				return num;
			}
			
		}
		
	//REBANLANCE BY  single bundle removal';
	public  HashMap<String,ArrayList<String>> rebalanceBySingleBundleRemoval(String name) throws IOException, EncodeException {
		
		synchronized(this) {
		
			HashMap<String, ArrayList<String>> returnValue = new HashMap<String, ArrayList<String>>();
		  if  ( ContextListener.getInstance().removeBundle(name) == true ) {
			  
			LinkedHashMap<String, NodeInfo> indexMap = theSessionClient.getSessionTable();
		
			int nodes = indexMap.size();
			int bundleSize  = existingBundles.size();
			
			if ( nodes > 0) {
				int requiredSize = (int)  Math.ceil((double)bundleSize/nodes);
				String targetKey = theSessionClient.findNodeByBundleName(name);
				String impactedKey = theSessionClient.findMaxSizeToken();
				logger.debug("targetKey = " + targetKey + " impactedKey = " + impactedKey);
				
				int curSize = theSessionClient.removeBundleByName(targetKey, name);
				if ( targetKey != null && impactedKey !=  null  ) {
					
					if ( targetKey.equals(impactedKey) ==  false ) {
						String tempBundle =  theSessionClient.removeBundleByPos(impactedKey, 0);
						theSessionClient.addBundleByName(targetKey, tempBundle);
					
						returnValue.put(targetKey, indexMap.get(targetKey).getBundles());
						returnValue.put(impactedKey, indexMap.get(impactedKey).getBundles());
						NotifyTarget(impactedKey);
					}
					NotifyTarget(targetKey);
				}
			}
		  }
		  return  returnValue;
		}
	}
	
	//Rebalance by nodes removal
    public  HashMap<String,ArrayList<String>> rebalanceByNodesRemoval(ArrayList<String> tokens)  {

        synchronized(this) {

                LinkedHashMap<String, NodeInfo> indexMap = theSessionClient.getSessionTable();
                HashMap<String, ArrayList<String>> returnValue = new HashMap<String, ArrayList<String>>();

                int nodes = indexMap.size();
                int bundles  = existingBundles.size();
                ArrayList<String> toBeReleasedBundles = new ArrayList<String>();

                for (String theToken:tokens) {
                	if (indexMap.get(theToken) != null) {
                        for (String  theBundle:indexMap.get(theToken).getBundles()) {
                                toBeReleasedBundles.add(theBundle);
                        }
                	}
                }

                int  releaseSize = toBeReleasedBundles.size();
                if ( nodes > tokens.size() && releaseSize > 0) {
                        int nodeToSize = (int)  Math.ceil((double)bundles/(nodes-tokens.size()));
                        int strongNodes = bundles % (nodes - tokens.size());

                        Iterator<String> iter =  indexMap.keySet().iterator();
                        int nodeID = 0, serialNo = 0;
                        while (iter.hasNext()) {
                                String key = iter.next();
                                if (tokens.contains(key) == false) {
                                        ArrayList<String> bundle = (ArrayList<String>) indexMap.get(key).getBundles();
                                        Session nodeSession = indexMap.get(key).getClient();
                                        for (int cursor = bundle.size(); cursor<nodeToSize; cursor++) {
                                                bundle.add(toBeReleasedBundles.get(serialNo));
                                                serialNo++;
                                        }

                                        NodeInfo node = new NodeInfo(key, nodeSession);
                                        node.setBundles(bundle);
                                        theSessionClient.replace(key, node);
                                        returnValue.put(key, bundle);

                                        if (++nodeID == strongNodes) nodeToSize--;
                                }  //skip the  node which would be removed
                                if ( serialNo == releaseSize ) break;
                        }

                }
                //close and remove all dead tokens finally
                for (String theToken:tokens) {
                        try {
							indexMap.get(theToken).getClient().close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        theSessionClient.remove(theToken);
                        eventTime.remove(theToken);
                }
                NotifyAll();
                return returnValue;
                }
    }

	private  void NotifyAll() {
		
	   	 LinkedHashMap<String, NodeInfo> allSessions = theSessionClient.getSessionTable();
    	 Iterator<String> cursor = (Iterator<String>) allSessions.keySet().iterator();
    	 int allNodes = theSessionClient.getSessionTable().size();
    	 
    	 //System.out.println("This no. of  nodes   = " + allNodes);
    	 while (cursor.hasNext()) {
    		 String  key  = cursor.next();
    		 try {
				allSessions.get(key).getClient().getBasicRemote().sendObject(new DaaPRange(key, allSessions.get(key).getBundles(),allNodes));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EncodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		 logger.info("Notification Message:" + key + " = " + allSessions.get(key).getBundles().toString() + " | allNodes = "  + allNodes );
    		
    	 }
	}
	
	private synchronized void NotifyTarget(String token) throws IOException, EncodeException{
		Session session = theSessionClient.getSessionTable().get(token).getClient();
		int allNodes = theSessionClient.getSessionTable().size();
		session.getBasicRemote().sendObject(new DaaPRange(token, theSessionClient.getSessionTable().get(token).getBundles(),allNodes));
	
		logger.info("To " + token + "notification Message:" + theSessionClient.getSessionTable().get(token).getBundles().toString() + " | allNodes = "  + allNodes );
	}
	
	public void NotifyAllNodes()  {
		synchronized(this) {
			NotifyAll();
		}
		
	}
}


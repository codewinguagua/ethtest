package com.pdxchg.coordinator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.websocket.EncodeException;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;
import com.pdxchg.coordinator.DaaPCoordinatorUtils;

public class HouseKeepingTask implements Runnable {
	
	private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);
	private long expireDuration = 45 * 1000;
	private long houseKeepingInterval = 60 * 1000;
	
	public void run(){
		
		if (ContextListener.getInstance().getProps().get("coordinator.heartbeatExpiration") != null )
			expireDuration = Long.parseLong((String) ContextListener.getInstance().
				getProps().get("coordinator.heartbeatExpiration"))*1000;
		
		if (ContextListener.getInstance().getProps().get("coordinator.housekeepingInterval") != null )
			houseKeepingInterval = Long.parseLong((String) ContextListener.getInstance().
				getProps().get("coordinator.housekeepingInterval"))*1000;
		
		logger.info("DaaP Coordinator housekeeping task begin...expireDuration = "
					+ "" + expireDuration + "  houseKeepingInterval = " + houseKeepingInterval);
		
		System.out.println("DaaP Coordinator housekeeping task begin...expireDuration = "
				+ "" + expireDuration + "  houseKeepingInterval = " + houseKeepingInterval);
		
		HazelcastInstance client = HazelcastClientUtils.getInstance().getClient();
		IMap<String, Long> timeStampMap = client.getMap(DaaPCoordinatorUtils.SESSION_TIMESTAMP_MAP);
		
		
		for(;;) {
				//get current time 
				
				    //ContextListener.getInstance().getLock().lock();
					Calendar now = Calendar.getInstance();
					long currentTime = now.getTimeInMillis();
					Iterator<String> iter = timeStampMap.keySet().iterator();
					ArrayList<String>  toBeClean = new ArrayList<String>();
					while (iter.hasNext()) {		
						String key = iter.next();
						if (timeStampMap.get(key) + expireDuration < currentTime ) {
							toBeClean.add(key);
						}
					}
					logger.info("HouseKeeping to be clean..." + toBeClean.toString());
				
				
					if (toBeClean.size()>0) {
						Rebalancing processing  = new Rebalancing();			
						//processing.notificationByNodesRemoval(toBeClean);
						processing.rebalanceByNodesRemoval(toBeClean);
					}
					//ContextListener.getInstance().getLock().unlock();
			
				try {
					Thread.sleep(houseKeepingInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


}

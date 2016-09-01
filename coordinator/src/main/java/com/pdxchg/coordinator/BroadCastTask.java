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

public  class BroadCastTask implements Runnable {
	
	private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);
	private long broadcastInterval = 60 * 1000;
	
	public void run(){
		
		if (ContextListener.getInstance().getProps().get("coordinator.broadcastInterval") != null )
			broadcastInterval = Long.parseLong((String) ContextListener.getInstance().
				getProps().get("coordinator.broadcastInterval"))*1000;
		
		logger.info("DaaP Coordinator broadcast task begin...broadcastDuration = "
				+ "" + " broadcastDuration =  "  + broadcastInterval);
		
		
		for(;;) {
			Rebalancing processing = new Rebalancing();
			processing.NotifyAllNodes(); 
			try {
				Thread.sleep(broadcastInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

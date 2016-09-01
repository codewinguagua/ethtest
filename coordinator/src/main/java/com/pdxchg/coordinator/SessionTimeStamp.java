package com.pdxchg.coordinator;

import java.util.ArrayList;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.pdxchg.coordinator.DaaPCoordinatorUtils;

public class SessionTimeStamp  {
	
	private long  timeStamp;
	private String token; 
	private HazelcastInstance client;
	
	public SessionTimeStamp(String token, long  timeStamp) {
		this.token = token;
		this.timeStamp = timeStamp;
		client = ContextListener.getInstance().getHazelClient();
	}
	
	public SessionTimeStamp() {
		client = ContextListener.getInstance().getHazelClient();
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public long getTimeStamp() {
		return this.timeStamp;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public void add() {
		IMap<String, Long> map = client.getMap(DaaPCoordinatorUtils.SESSION_TIMESTAMP_MAP);
		map.put(token, timeStamp);
	}
	
	public void updateStatus() {
		IMap<String, Long> map = client.getMap(DaaPCoordinatorUtils.SESSION_TIMESTAMP_MAP);
		map.replace(token, timeStamp);
	}
	
	public void remove() {
		IMap<String, Long> map = client.getMap(DaaPCoordinatorUtils.SESSION_TIMESTAMP_MAP);
		map.remove(token);
	}
	
	public void remove(String key) {
		IMap<String, Long> map = client.getMap(DaaPCoordinatorUtils.SESSION_TIMESTAMP_MAP);
		map.remove(key);
	}
	
}

package com.pdxchg.coordinator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.websocket.Session;

public class Sessions {
	private LinkedHashMap<String, NodeInfo> sessionTable = new LinkedHashMap<String, NodeInfo>();
	
	private static Sessions inst = null;

	public static synchronized  Sessions getInstance() {
			if  (inst == null)
			inst = new Sessions();
		return inst;
	}

	private Sessions() {
	}
	
	public synchronized void add(String token, NodeInfo node) {
		sessionTable.put(token, node);
	}
	
	public synchronized  void remove(String token) {
		sessionTable.remove(token);
	}
	
	public synchronized void  replace(String token, NodeInfo node) {
		sessionTable.put(token, node);	
	}
	
	public LinkedHashMap<String,NodeInfo> getSessionTable() {
		return sessionTable;
	}
	
	public  boolean contains(String token) {
		return sessionTable.containsKey(token);
	}
	
	
	public String isAlreadyIn(String ipStr) {
		String rtn = null;
		
			Iterator<String> iter = sessionTable.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (DaaPCoordinatorUtils.getIP(key).equals(ipStr)) {
					rtn = key;
					break;
				}
			}
		return rtn;
	}
	
	public String findNodeByBundleName(String  name) {
		String token = null;
		Iterator<String> iter = sessionTable.keySet().iterator();	
		while (iter.hasNext()) {
			String key = iter.next();
			if (sessionTable.get(key).getBundles().contains(name)) {
				token = key;
				break;
			}
		}
		return token ;
	}
	
	public String findMaxSizeToken()  {
		String token = null;
		Iterator<String> iter = sessionTable.keySet().iterator();
		int requiredSize = 0;
		while (iter.hasNext()) {
			String key = iter.next();
			if (sessionTable.get(key).getBundles().size() > requiredSize ) {
				token = key;
				requiredSize = sessionTable.get(key).getBundles().size();
			}
		}
		return token;
	}
	
	public int  removeBundleByName(String token, String name) {
		sessionTable.get(token).getBundles().remove(name);
		return sessionTable.get(token).getBundles().size();
	}
	
	public String removeBundleByPos(String  token, int pos) {
		return sessionTable.get(token).getBundles().remove(pos);
	}
	
	public void addBundleByName(String token, String name) {
		sessionTable.get(token).getBundles().add(name);
	}
	
	public boolean verifyToken(String token) {
		return sessionTable.containsKey(token);			
	}
}

package com.pdxchg.coordinator;

import java.util.ArrayList;

import javax.websocket.Session;



public class NodeInfo {
	
	private String  token;
	private Session client;
    private ArrayList<String> bundlePool = new ArrayList<String>();
	
	
	public NodeInfo(String token, Session  client) {
		this.token  = token;
		this.client = client;		
	}
	
	public NodeInfo() {
		
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public void setSession(Session client)  {
		this.client = client;
	}
	
	public void  setBundles(ArrayList<String> bundles) {
	   bundlePool.addAll(bundles);	
	}
	
	public String getToken() {
		return this.token;
	}
	
	public Session getClient() {
		return this.client;
	}
	
	public ArrayList<String> getBundles() {
		return this.bundlePool;
	}

}

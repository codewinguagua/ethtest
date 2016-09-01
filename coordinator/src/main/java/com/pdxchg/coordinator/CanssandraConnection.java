package com.pdxchg.coordinator;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CanssandraConnection {
	
	private static Cluster cluster = null;
	private static Session session = null;
	public static String DAAP_KEY_SPACE;
	
	 
	
	private CanssandraConnection() {
		
	}
	
	public static void init() {
		
		String username = "daap";
		String password = "daap";
		String node = "192.168.3.73";
		String port = "9042";
		username = ContextListener.getInstance().getProps().getProperty("canssandra.username");
		password = ContextListener.getInstance().getProps().getProperty("canssandra.password");
		node = ContextListener.getInstance().getProps().getProperty("canssandra.host");
		port = ContextListener.getInstance().getProps().getProperty("canssandra.port");
		DAAP_KEY_SPACE = ContextListener.getInstance().getProps().getProperty("daap_keyspace");
		
		
		if (session != null) session.close();
		if (cluster != null) cluster.close();
		cluster = Cluster.builder().withCredentials(username, password).addContactPoints(node).build();
		session = cluster.connect();
	}
	
	public synchronized static Session getConnection() {
		if  (cluster == null || session == null ) {
			init();
		}
		return session;
	}
	
	public static synchronized void release() {
		session.close();
		cluster.close();
	}

}

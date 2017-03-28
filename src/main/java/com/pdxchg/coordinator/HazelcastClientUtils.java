package com.pdxchg.coordinator;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HazelcastClientUtils {

	private static HazelcastClientUtils instance  = null;;
	
	public static HazelcastClientUtils getInstance() {
		if (instance == null)
			instance = new HazelcastClientUtils();
		return instance;
	}
	
	
	public HazelcastInstance getClient(){
		
		//read configuration file
	String pdx_root_path = System.getenv("PDX_HOME");
		
		String path = pdx_root_path + "/etc/pdx.properties";
		Properties props = readProperties(path);
		
		
		 ClientConfig clientConfig = new ClientConfig();

		 //String hazelHost = "192.168.2.56:5701";
		 String hazelHost;
		 hazelHost = props.getProperty("hazelcastip");
         clientConfig.getNetworkConfig().addAddress(hazelHost);
         HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);  
        // IMap map = client.getMap("customers");
         //map.set("wen", "wenvalue");
        // System.out.println("Map Size:" + map.size());
        // System.out.println(map.get("wen"));
         return client;
	}
	
	
	private static Properties readProperties(String filePath) {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(filePath));
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}

}

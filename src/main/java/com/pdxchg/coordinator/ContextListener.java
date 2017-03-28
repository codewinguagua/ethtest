package com.pdxchg.coordinator;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;
import com.pdxchg.coordinator.DaaPCoordinatorUtils;

/**
 * 容器启动监听操作，1.clear之前维护的sessions 2.加载配置文件数据到内存
 * 
 * @author fohq
 */

public class ContextListener implements ServletContextListener {
	private  static Properties props ;
	public  static  ContextListener cache;
	private static ArrayList<String> bundleList;
	private static  HazelcastInstance _client = HazelcastClientUtils.getInstance().getClient();
	//private static  Lock  hlock = new ReentrantLock();
	
	public static  ContextListener getInstance(){
		
		if(cache == null){
			cache = new ContextListener();
		}
		return cache;
	}
	
	/*
	public Lock  getLock() {
		return  this.hlock;
	}*/
	
	private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);
	
	public void contextInitialized(ServletContextEvent sce) {
 
		//read configuration
		String pdx_root_path = System.getenv("PDX_HOME");
		
		String path = pdx_root_path + "/etc/pdx.properties";
		Properties props = readProperties(path);
		
		this.setProps(props);
		Enumeration en = props.propertyNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String Property = props.getProperty(key);
			logger.info(key + ":" + Property);
		}
		
		//_client = HazelcastClientUtils.getInstance().getClient(); 
		//Clear hazelcast session map
		IMap<String, Long> timeStampMap = _client.getMap(DaaPCoordinatorUtils.SESSION_TIMESTAMP_MAP);
		logger.info("hazelcast--timestamp map--" + timeStampMap.size());
		timeStampMap.clear();

		//get all  bundles from PDX DAPP self service module
		bundleList = new ArrayList<String>();
		for (int i = 0; i<10; i++) bundleList.add("daap://" +  "xuying/" + "Dummy" + Integer.toString(i));  //just test,need to  implement"
		try {
			remoteDapps dapp  = new remoteDapps();
			dapp.loadDappLists();
		} catch (SQLException e) {
			 e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//start a housekeeping task thread
		
		ExecutorService  houseKeeping = Executors.newSingleThreadExecutor();
		houseKeeping.execute(new HouseKeepingTask());
		
		
		//start a broadcast task thread
		ExecutorService broadcast  = Executors.newSingleThreadExecutor();
		broadcast.execute(new BroadCastTask());
		
	}

	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("listener destroy");
	}

	// 读取properties的全部信息
	public static Properties readProperties(String filePath) {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(filePath));
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}

	public HazelcastInstance getHazelClient()  {
		return _client;
	}
	
	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public ArrayList<String> getBundleList() {
		return this.bundleList;
	}
	
	public synchronized  void addBundles(ArrayList<String> newBundles) {
		for (String theBundle:newBundles) {
			bundleList.add(theBundle);
		}
	}
	
	public synchronized boolean removeBundle(String bundle) {
			return bundleList.remove(bundle);
	}
}

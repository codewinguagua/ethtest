package com.pdxchg.coordinator;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.pdxchg.log.extend.PDXLogger;
import com.pdxchg.log.extend.PDXLoggerFactory;


// select A.userAccountId, C.url from user A, bundle B, dapp C where A.id =  B.uid and B.id = C.bid
public class remoteDapps   {
  private Connection conn; 
  private PDXLogger logger = PDXLoggerFactory.getLogger(ContextListener.class);
  
  public remoteDapps() throws ClassNotFoundException, SQLException {
	  String url = ContextListener.getInstance().getProps().getProperty("jdbc.url");
	  String user = ContextListener.getInstance().getProps().getProperty("jdbc.username");
	  String  password = ContextListener.getInstance().getProps().getProperty("jdbc.password");
	  String className = ContextListener.getInstance().getProps().getProperty("jdbc.driverClassName");
	  //System.out.println("url = " + url + " username = " + user + " password = " + password + " className " + className);se
	  Class.forName(className); 
      conn = (Connection) DriverManager.getConnection(url, user, password);  
  }
  
  public void loadDappLists() throws SQLException {
	  ArrayList<String> bundles = ContextListener.getInstance().getBundleList();
	  
	  String getListSql = "select A.userAccountId, C.url from user A, "
	  		+ "bundle B, dapp C where A.id =  B.uid and B.id = C.bid and B.type = 'remote'";
	 PreparedStatement stmt =  (PreparedStatement) conn.prepareStatement(getListSql);
	 ResultSet resultSet=stmt.executeQuery();
	 System.out.println("Remote Dapps:");
	 logger.info("remote-dapps-lists:");
	 while(resultSet.next()) {
		 String remoteDapp = "daap://" + resultSet.getString(1)  +  resultSet.getString(2);
		 ContextListener.getInstance().getBundleList().add(remoteDapp);
		 logger.info(remoteDapp + "    ");
		 System.out.println(remoteDapp + " ");
	 }
	 stmt.close();
	 resultSet.close();
	 conn.close();
	  
  }
  
}

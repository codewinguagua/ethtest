package com.pdxchg.coordinator;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;

public class NodePubKey {
	private Session conn;
	private String nodeIP;
	private String  publicKey;
	private long timeStamp;
	
	public NodePubKey(String nodeIP, String publicKey, long timeStamp) {
		this.nodeIP = nodeIP;
		this.publicKey = publicKey;
		this.timeStamp = timeStamp;
		conn = CanssandraConnection.getConnection();
	}
	public String getNodeIP() {
		return  this.nodeIP;
	}
	
	public String  getPublicKey() {
		return this.publicKey;
	}
	
	public long getTimeStamp() {
		return this.timeStamp;
	}
	
	public void add() {
		
		//only register at the startup, otherwise  ignore 
		ResultSet result = conn.execute(QueryBuilder.select("nodeid")
				.from(CanssandraConnection.DAAP_KEY_SPACE, "node_pubkey")
				.where(QueryBuilder.eq("nodeid", nodeIP)));

		if ( result.isExhausted() == true) {
			conn.execute(QueryBuilder.insertInto("daap", "node_pubkey")
			        .values(new String[]{"nodeid","publickey","timestamp"}, 
			        		new Object[]{nodeIP, publicKey, String.valueOf(this.timeStamp)}));
					
		}
	}
	
	public void remove() {
		conn.execute(QueryBuilder.delete()
				   .from(CanssandraConnection.DAAP_KEY_SPACE, "node_pubkey")
				   .where(QueryBuilder.eq("nodeid", nodeIP)));
	}
}

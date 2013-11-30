package com.balloonpi.xbeed;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XbeedQueue {
	private static Logger logger = LogManager.getLogger();
	
	static private Queue<Byte[]> txQueue = new LinkedList<>();
	static private Map<String, Queue<Byte[]>> rxQueueMap = new HashMap<String, Queue<Byte[]>>();
	
	static public boolean setTx(Byte[] data){
		return txQueue.offer(data);
	}
	
	static public Byte[] getTx(){
		return txQueue.poll();
	}
	
	static public void setRx(Byte[] data){
		Iterator<String> it = rxQueueMap.keySet().iterator();
		while(it.hasNext()){
			rxQueueMap.get(it.next()).offer(data);
		}
	}
	
	static public Byte[] getRx(String id){
		Queue<Byte[]> que = rxQueueMap.get(id);
		if( que == null){
			logger.debug("There is no id ({}) in RX queue map",id);
			return null;
		}
		
		return que.poll();
	}
	
	static public void regist(String id){
		Queue<Byte[]> que = new LinkedList<>();
		rxQueueMap.put(id, que);
	}
	
	static public void delete(String id){
		rxQueueMap.remove(id);
	}
}

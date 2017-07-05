package com.so_cili.dhtcrawler.main;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.so_cili.dhtcrawler.handler.AnnouncePeerInfoHashWireHandler;
import com.so_cili.dhtcrawler.listener.OnAnnouncePeerListener;
import com.so_cili.dhtcrawler.listener.OnGetPeersListener;
import com.so_cili.dhtcrawler.server.DHTServer;
import com.so_cili.dhtcrawler.structure.DownloadPeer;
import com.so_cili.dhtcrawler.structure.MyQueue;
import com.so_cili.dhtcrawler.util.ByteUtil;

public class Main {
	
	
	public static void main(String[] args) throws Exception{
		
		DHTServer server = new DHTServer(45666, 300000);
		AnnouncePeerInfoHashWireHandler announcePeerInfoHashWireHandler = new AnnouncePeerInfoHashWireHandler();
		ExecutorService writeTorrentFileThreadPool = Executors.newFixedThreadPool(5);
		
		//配置get_peer请求监听器
		server.setOnGetPeersListener(new OnGetPeersListener() {
			
			@Override
			public void onGetPeers(InetSocketAddress address, byte[] info_hash) {
				System.out.println("get_peers request, address:" + address.getHostString() + ", info_hash:" + ByteUtil.byteArrayToHex(info_hash));
			}
		});

		//配置announce_peers请求监听器
		server.setOnAnnouncePeerListener(new OnAnnouncePeerListener() {
			 
			@Override
			public void onAnnouncePeer(InetSocketAddress address, byte[] info_hash, int port) {
				
				writeTorrentFileThreadPool.submit(new Runnable() {
					public void run() {
						try{
							announcePeerInfoHashWireHandler.handler(address, info_hash);
						}catch (Exception e){
							System.out.println("处理 announce_peer 时错误");
						}
					}
				});
			}
		});
		
		server.start();
	}
	
	
}

package components;

import utilities.Utilities;
import utilities.VideoFile;
import utilities.VideoFileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Broker implements Comparable<Broker> {
	
	private final int portToPublishers, portToConsumers;
	private final String ip;
	ServerSocket brokerSocketToPublishers, brokerSocketToConsumers;
	Socket connection = null;
	BigInteger hashValue;
	private Vector<ToPublisherThread> publishers;
	private HashMap<String, VideoFile> videoFiles;
	
	/**
	 * @param ip
	 * @param portToPublishers
	 * @param portToConsumers
	 */
	public Broker(String ip, int portToPublishers, int portToConsumers) {
		this.ip = ip;
		this.portToPublishers = portToPublishers;
		this.portToConsumers = portToConsumers;
		this.hashValue = Utilities.hash(this.ip, this.portToPublishers);
		this.publishers = new Vector<ToPublisherThread>();
		this.videoFiles = new HashMap<String, VideoFile>();
	}
	
	public int getPortToPublishers() {
		return portToPublishers;
	}
	
	public int getPortToConsumers() {
		return portToConsumers;
	}
	
	public String getIp() {
		return ip;
	}
	
	public BigInteger getHashValue() {
		return hashValue;
	}
	
	public ServerSocket getBrokerSocketToPublishers() {
		return brokerSocketToPublishers;
	}
	
	public ServerSocket getBrokerSocketToConsumers() {
		return brokerSocketToConsumers;
	}
	
	public Socket getConnection() {
		return connection;
	}
	
	public Vector<ToPublisherThread> getPublishers() {
		return publishers;
	}
	
	public HashMap<String, VideoFile> getVideoFiles() {
		return videoFiles;
	}
	
	public void runBroker() {
		System.out.println("Running Broker with ip " + ip + " and ports " + portToPublishers + ", " + portToConsumers);
		receiveData();
	}
	
	private void receiveData() {
		
		new Thread("Data-receiving Thread") {
			@Override
			public void run() {
				while (true) {
					try {
						InetAddress addr = InetAddress.getByName(ip);
						brokerSocketToPublishers = new ServerSocket(portToPublishers, 50, addr);
						Socket socket = brokerSocketToPublishers.accept();
						ToPublisherThread newPublisher = new ToPublisherThread(socket);
						newPublisher.start();
						synchronized (publishers) {
							publishers.addElement(newPublisher);
						}
						brokerSocketToPublishers.close();
					} catch (IOException e) {
						System.out.println("Failed to establish publisher-broker connection");
						// e.printStackTrace();
					}
				}
			}
		}.start();
		
	}
	
	private class ToPublisherThread extends Thread {
		public Socket socket;
		private ObjectInputStream ois;
		
		public ToPublisherThread(Socket socket) {
			this.socket = socket;
			try {
				InputStream is = socket.getInputStream();
				ois = new ObjectInputStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				ArrayList<VideoFile> chunks = new ArrayList<VideoFile>();
				boolean endFound = false;
				System.out.println("Getting files");
				while (!endFound) {
					VideoFile chunk = (VideoFile) ois.readObject();
					chunks.add(chunk);
					endFound = chunk.isFinal();
				}
				VideoFile res = VideoFileHandler.merge(chunks);
				synchronized (videoFiles) {
					videoFiles.put(res.getName(), res);
					VideoFileHandler.writeFile(res, System.getProperty("user.dir") + "/Broker" + getIp() +
							getPortToPublishers() + getPortToConsumers());
				}
				System.out.println("Successfully merged and saved file");
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int compareTo(Broker o) {
		return this.hashValue.compareTo(o.getHashValue());
	}
}

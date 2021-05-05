package components;

import utilities.Utilities;
import utilities.VideoFile;
import utilities.VideoFileHandler;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Broker implements Comparable<Broker> {
	
	private final int portToPublishers, portToConsumers;
	private final String ip;
	ServerSocket brokerSocketToPublishers, brokerSocketToConsumers;
	Socket connection = null;
	BigInteger hashValue;
	private Vector<ToPublisherThread> publishers;
	private Vector<ToConsumerThread> consumers;
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
		this.consumers = new Vector<ToConsumerThread>();
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
		sendData();
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
					VideoFileHandler.writeFile(res, System.getProperty("user.dir") + "/Broker" + getIp()
							+ getPortToPublishers() + getPortToConsumers());
					res.setData(null);
					videoFiles.put(res.getName(), res);
				}
				System.out.println("Successfully merged and saved file");
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendData() {
		new Thread("Data-sending Thread") {
			@Override
			public void run() {
				while (true) {
					try {
						InetAddress addr = InetAddress.getByName(ip);
						brokerSocketToConsumers = new ServerSocket(portToConsumers, 50, addr);
						Socket socket = brokerSocketToConsumers.accept();
						ToConsumerThread newConsumer = new ToConsumerThread(socket);
						newConsumer.start();
						synchronized (consumers) {
							consumers.addElement(newConsumer);
						}
						brokerSocketToConsumers.close();
					} catch (IOException e) {
						System.out.println("Failed to establish consumer-broker connection");
						// e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	private class ToConsumerThread extends Thread {
		public Socket socket;
		private ObjectInputStream oins;
		private ObjectOutputStream oouts;
		
		public ToConsumerThread(Socket socket) {
			this.socket = socket;
			try {
				InputStream is = socket.getInputStream();
				oins = new ObjectInputStream(is);
				OutputStream os = socket.getOutputStream();
				oouts = new ObjectOutputStream(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				String searchedWord = oins.readUTF();
				String byWho = oins.readUTF();
				if (searchedWord.charAt(3) == '#') {
					int cnt = 0;
					String hashtag = searchedWord.replace("#", "").replace("in:", "").toLowerCase().trim();
					String clientName = byWho.replace("by:", "").trim();
					for (VideoFile vf : videoFiles.values()) {
						if (vf.getHashtags().contains(hashtag) && !vf.getChannel().trim().equals(clientName)) {
							cnt += 1;
						}
					}
					oouts.writeInt(cnt);
					oouts.flush();
					// TODO: read whether you should send or not. If yes send, otherwise don't
					boolean shouldISend = oins.readBoolean();
					if (shouldISend) {
						int indexToSend = new Random().nextInt(cnt);
						VideoFile toSend = null;
						for (VideoFile vf : videoFiles.values()) {
							if (vf.getHashtags().contains(hashtag) && !vf.getChannel().trim().equals(clientName)) {
								if (indexToSend == 0) {
									toSend = vf;
									break;
								} else {
									indexToSend -= 1;
								}
							}
						}
						String directory = System.getProperty("user.dir") + "/Broker" + ip + portToPublishers
								+ portToConsumers + "/";
						toSend = VideoFileHandler.readFile(directory + toSend.getName(), toSend.getChannel());
						ArrayList<VideoFile> result = VideoFileHandler.split(toSend);
						for (VideoFile x : result) {
							oouts.writeObject(x);
							oouts.flush();
						}
					}
					oouts.close();
					socket.close();
				} else {
					ArrayList<VideoFile> maybeToSend = new ArrayList<>();
					String requestedChannel = searchedWord.replace("in:", "").toLowerCase();
					for (VideoFile vf : videoFiles.values()) {
						if (vf.getChannel().trim().equalsIgnoreCase(requestedChannel.trim())) {
							maybeToSend.add(vf);
						}
					}
					if (maybeToSend.isEmpty()) {
						VideoFile toSend = new VideoFile("EMPTY", null, new ArrayList<>(), 0, true);
						oouts.writeObject(toSend);
						oouts.flush();
					} else {
						int randIndex = new Random().nextInt(maybeToSend.size());
						VideoFile toSend = maybeToSend.get(randIndex);
						String directory = System.getProperty("user.dir") + "/Broker" + ip + portToPublishers
								+ portToConsumers + "/";
						toSend = VideoFileHandler.readFile(directory + toSend.getName(), requestedChannel.trim());
						ArrayList<VideoFile> result = VideoFileHandler.split(toSend);
						for (VideoFile x : result) {
							oouts.writeObject(x);
							oouts.flush();
						}
					}
					oouts.close();
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public int compareTo(Broker o) {
		return this.hashValue.compareTo(o.getHashValue());
	}
}

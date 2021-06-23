package e.master.updog.components;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import e.master.updog.utilities.Pair;
import e.master.updog.utilities.Utilities;
import e.master.updog.utilities.VideoFile;
import e.master.updog.utilities.VideoFileHandler;

public class Broker implements Comparable<Broker> {
	
	private final int portToPublishers, portToConsumers;
	private final String ip;
	ServerSocket brokerSocketToPublishers, brokerSocketToConsumers;
	Socket connection = null;
	BigInteger hashValue;
	private Vector<ToPublisherThread> publishers;
	private Vector<ToConsumerThread> consumers;
	private HashMap<String, VideoFile> videoFiles;
	private HashMap<String, ArrayList<Pair>> brokersToHashtags;
	private ArrayList<Broker> otherBrokers;
	
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
		this.publishers = new Vector<>();
		this.consumers = new Vector<>();
		this.videoFiles = new HashMap<>();
		this.brokersToHashtags = new HashMap<>();
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
	
	public void setOtherBrokers(ArrayList<Broker> otherBrokers) {
		this.otherBrokers = new ArrayList<>();
		for (Broker b : otherBrokers) {
			Broker tmp = new Broker(b.getIp(), b.getPortToPublishers(), b.getPortToConsumers());
			this.otherBrokers.add(tmp);
			this.brokersToHashtags.put(b.getString(), new ArrayList<>());
		}
	}
	
	/**
	 * Runs a method to connect with the Publishers and receive data from them, and a method to send data to users
	 */
	public void runBroker() {
		System.out.println("Running Broker with ip " + ip + " and ports " + portToPublishers + ", " + portToConsumers);
		System.out.println("Broker's hash value: " + hashValue);
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
				boolean isVideoFile = ois.readBoolean();
				if (isVideoFile) {
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
					Pair toPut;
					synchronized (brokersToHashtags) {
						for (String htg : res.getHashtags()) {
							toPut = new Pair(htg, res.getChannel());
							brokersToHashtags.get(getString()).add(toPut);
						}
					}
					for (Broker br : otherBrokers) {
						if (br.getString().equals(getString())) {
							continue;
						}
						String ipToSpeak = br.getIp();
						int portToSpeak = br.getPortToPublishers();
						Socket tmpSocket = new Socket(ipToSpeak, portToSpeak);
						OutputStream tmpOutStream = tmpSocket.getOutputStream();
						ObjectOutputStream tmpObjectOutStream = new ObjectOutputStream(tmpOutStream);
						tmpObjectOutStream.writeBoolean(false);
						tmpObjectOutStream.flush();
						tmpObjectOutStream.writeObject(brokersToHashtags);
						tmpObjectOutStream.flush();
						tmpSocket.close();
						tmpOutStream.close();
						tmpObjectOutStream.close();
					}
				} else {
					Object tmpObject = ois.readObject();
					if (tmpObject == null) {
						System.out.println("Something went wrong updating brokers to hashtags HashMap");
					} else {
						brokersToHashtags = (HashMap<String, ArrayList<Pair>>) tmpObject;
					}
				}
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
				// If user searched by hashtag
				if (searchedWord.equals("GETBROKERLIST")) {
					for (Broker x : otherBrokers) {
						oouts.writeUTF(x.getString());
						oouts.flush();
					}
					oouts.writeUTF("FINISHED");
					oouts.flush();
				} else if (searchedWord.charAt(3) == '#') {
					String byWho = oins.readUTF();
					// count how many videos not belonging to client, you have with this hashtag
					int cnt = 0;
					String hashtag = searchedWord.replace("#", "").replace("in:", "").toLowerCase().trim();
					String clientName = byWho.replace("by:", "").trim();
					for (VideoFile vf : videoFiles.values()) {
						if (vf.getHashtags().contains(hashtag) && !vf.getChannel().trim().equals(clientName)) {
							cnt += 1;
						}
					}
					if (cnt > 0) {
						oouts.writeUTF("VIDEO");
						oouts.flush();
						// A random integer in [0, cnt-1]. This is the "count" of the video to send
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
						// read video from the correct place
						String directory = System.getProperty("user.dir") + "/Broker" + ip + portToPublishers
								+ portToConsumers + "/";
						toSend = VideoFileHandler.readFile(directory + toSend.getName(), toSend.getChannel());
						// and send it
						ArrayList<VideoFile> result = VideoFileHandler.split(toSend);
						for (VideoFile x : result) {
							oouts.writeObject(x);
							oouts.flush();
						}
					} else {
						String where = null;
						outer:
						for (String key : brokersToHashtags.keySet()) {
							ArrayList<Pair> pairs = brokersToHashtags.get(key);
							for (Pair p : pairs) {
								if (p.getHashtag().equals(hashtag) && !p.getChannel().equals(clientName)) {
									where = key;
									break outer;
								}
							}
						}
						if (where != null) {
							oouts.writeUTF(where);
							oouts.flush();
						} else {
							oouts.writeUTF("NOT FOUND");
							oouts.flush();
						}
					}
				} else { //if user searched by channel
					// candidate videos for sending
					ArrayList<VideoFile> maybeToSend = new ArrayList<>();
					String requestedChannel = searchedWord.replace("in:", "").toLowerCase();
					for (VideoFile vf : videoFiles.values()) {
						if (vf.getChannel().trim().equalsIgnoreCase(requestedChannel.trim())) {
							maybeToSend.add(vf);
						}
					}
					// if there are no videos by specified channel
					if (maybeToSend.isEmpty()) {
						VideoFile toSend = new VideoFile("EMPTY", null, new ArrayList<>(), 0, true);
						oouts.writeObject(toSend);
						oouts.flush();
					} else { // if we have videos to send choose randomly one
						int randIndex = new Random().nextInt(maybeToSend.size());
						VideoFile toSend = maybeToSend.get(randIndex);
						// read from the right place
						String directory = System.getProperty("user.dir") + "/Broker" + ip + portToPublishers
								+ portToConsumers + "/";
						toSend = VideoFileHandler.readFile(directory + toSend.getName(), requestedChannel.trim());
						// and send it
						ArrayList<VideoFile> result = VideoFileHandler.split(toSend);
						for (VideoFile x : result) {
							oouts.writeObject(x);
							oouts.flush();
						}
					}
				}
				oouts.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public int compareTo(Broker o) {
		return this.hashValue.compareTo(o.getHashValue());
	}
	
	public String getString() {
		return this.getIp() + ";" + this.getPortToPublishers() + ";" + this.getPortToConsumers();
	}
	
}

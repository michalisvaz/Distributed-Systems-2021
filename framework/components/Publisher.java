package components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import utilities.Utilities;
import utilities.VideoFile;
import utilities.VideoFileHandler;

public class Publisher {
	
	private String IP, brokerIP = null;
	private String channelName;
	Socket pubSocket = null;
	ObjectOutputStream pubOutputStream = null;
	ObjectInputStream pubInputStream = null;
	private int port, brokerPort = 0;
	private VideoFile currentVideo = null;
	
	/**
	 * Maybe more properties will be needed
	 *
	 * @param IP          ip address of Publisher
	 * @param channelName name of Publisher's channel
	 * @param port        port number of the Publisher
	 */
	public Publisher(String IP, String channelName, int port) {
		this.IP = IP;
		this.port = port;
		this.channelName = channelName;
	}
	
	/**
	 * sets brokerIP and brokerPort
	 *
	 * @param brokers list of brokers. Assume it is sorted
	 * @return true if everything went ok, false otherwise
	 */
	public boolean init(ArrayList<Broker> brokers) {
		if (brokers == null || brokers.isEmpty()) {
			return false;
		}
		BigInteger myHash = Utilities.hash(channelName);
		boolean found = false;
		for (Broker broker : brokers) {
			if (myHash.compareTo(broker.getHashValue()) < 0) {
				this.brokerIP = broker.getIp();
				this.brokerPort = broker.getPortToPublishers();
				found = true;
				break;
			}
		}
		if (!found) {
			this.brokerIP = brokers.get(0).getIp();
			this.brokerPort = brokers.get(0).getPortToPublishers();
		}
		return true;
	}
	
	public void readFile() {
		currentVideo = null;
		System.out.println("Give the name of the file you wish to upload");
		Scanner sc = new Scanner(System.in);
		String videoName = sc.nextLine();
		if (videoName.equals("CANCEL")) {
			currentVideo = null;
			return;
		}
		currentVideo = VideoFileHandler.readFile(videoName, channelName);
		while (currentVideo == null) {
			System.out.println("Video no found. Try again. Type CANCEL to cancel your video upload.");
			sc = new Scanner(System.in);
			videoName = sc.nextLine();
			if (videoName.equals("CANCEL")) {
				currentVideo = null;
				return;
			}
			currentVideo = VideoFileHandler.readFile(videoName, channelName);
		}
	}
	
	public boolean sendVideo() {
		
		try {
			pubSocket = new Socket(brokerIP, brokerPort);
			pubOutputStream = new ObjectOutputStream(pubSocket.getOutputStream());
			pubInputStream = new ObjectInputStream(pubSocket.getInputStream());

			// pubOutputStream.writeBytes(channelName);
			// pubOutputStream.flush();
			
			ArrayList<VideoFile> result = VideoFileHandler.split(currentVideo);
			for (VideoFile x : result) {
				pubOutputStream.writeObject(x);
			}
			pubInputStream.close();
			pubOutputStream.close();
			pubSocket.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String getIP() {
		return IP;
	}
	
	public String getChannelName() {
		return channelName;
	}
	
	public int getPort() {
		return port;
	}
	
	public void getBrokerList() {
	
	}
	
	public VideoFile getCurrentVideo() {
		return currentVideo;
	}
	
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	
}

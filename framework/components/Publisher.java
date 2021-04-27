package components;

import utilities.Utilities;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Publisher {

	private final String IP;
	private String channelName;
	private List<String> hashtags;
	ServerSocket pubServerSocket = null;
	Socket pubSocket = null;
	ObjectOutputStream pubOutputStream = null;
	ObjectInputStream pubInputStream = null;
	private final int port, brokerPort;

	/**
	 * Maybe more properties will be needed
	 *
	 * @param IP          ip address of Publisher
	 * @param channelName name of Publisher's channel
	 * @param port        port number of the Publisher
	 * @param hashtags    the hashtags for which Publisher has at least one video
	 */
	public Publisher(String IP, String channelName, int port, List<String> hashtags) {
		this.IP = IP;
		this.port = port;
		this.brokerPort = Utilities.BROKER_PORT_TO_PUB;
		this.channelName = channelName;
		this.hashtags = new ArrayList<String>();
		for (String s : hashtags) {
			this.hashtags.add(s);
		}
	}

	public void connect() {
		
		
		pubSocket = new Socket(InetAddress.getByName("127.0.0.1"), brokerPort);
		
		pubOutputStream = new ObjectOutputStream(pubSocket.getOutputStream()); 
		pubInputStream = new ObjectInputStream(pubSocket.getInputStream());
		
		try {
			pubOutputStream.writeObject(channelName); //publisher sends first his name
			pubOutputStream.flush();
			
			for (String s : hashtags) {
				pubOutputStream.writeObject(s);		//then the hashtags
				pubOutputStream.flush();
			}
			pubInputStream.close();
			pubOutputStream.close();
			pubSocket.close();
			
			pubServerSocket = new ServerSocket(port);
			
			while(true) {
			pubSocket = pubServerSocket.accept();
			
			Thread t = new ClientHandler(pubSocket); //isws prepei allos handler gia publisher allos gia broker

            t.start();
			}
		}
	}

	public String getIP() {
		return IP;
	}

	public String getChannelName() {
		return channelName;
	}

	public List<String> getHashtags() {
		return hashtags;
	}

	public int getPort() {
		return port;
	}

	public void getBrokerList() {

	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void setHashtags(List<String> hashtags) {
		this.hashtags = new ArrayList<String>();
		for (String s : hashtags) {
			this.hashtags.add(s);
		}
	}

}

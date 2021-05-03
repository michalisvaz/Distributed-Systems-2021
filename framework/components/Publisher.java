package components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	private final String IP, brokerIP = null;
	private String channelName;
	Socket pubSocket = null;
	ObjectOutputStream pubOutputStream = null;
	ObjectInputStream pubInputStream = null;
	private final int port, brokerPort = 0;
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
	
	public boolean init(){
		// TODO: initialize sockets, find to which broker you should send (by hashing)
	}

	public void readFile(){
		currentVideo = null;
		System.out.println("Give the name of the file you wish to upload");
		Scanner sc = new Scanner(System.in);
		String videoName = sc.nextLine();
		if(videoName.equals("CANCEL")){
			currentVideo = null;
			return;
		}
		currentVideo = VideoFileHandler.readFile(videoName, channelName);
		while (currentVideo == null){
			System.out.println("Video no found. Try again. Type CANCEL to cancel your video upload.");
			sc = new Scanner(System.in);
			videoName = sc.nextLine();
			if(videoName.equals("CANCEL")){
				currentVideo = null;
				return;
			}
			currentVideo = VideoFileHandler.readFile(videoName, channelName);
		}
	}
	
	public boolean sendVideo() {

		try {
			pubSocket = new Socket(InetAddress.getByName("127.0.0.1"), brokerPort); // connects with broker to announce
																					// existance

			pubOutputStream = new ObjectOutputStream(pubSocket.getOutputStream());
			pubInputStream = new ObjectInputStream(pubSocket.getInputStream());

			pubOutputStream.writeBytes(channelName);
			pubOutputStream.flush();

			// see stella's init method and what it calls
			
			// TODO: send the chunks of the video to the correct broker
			// List result = split
//			for(x in result){
//				send x
//			}
			

			pubInputStream.close();
			pubOutputStream.close();
			pubSocket.close();

			/*
			 * ------------------We won't use this probably-----------
			 * 
			 * pubServerSocket = new ServerSocket(port);
			 * 
			 * while (true) { pubSocket = pubServerSocket.accept();
			 * 
			 * Thread t = new ClientHandler(pubSocket); // isws prepei allos handler gia
			 * publisher allos gia broker
			 * 
			 * t.start(); }
			 */
		} catch (IOException e) {
			e.printStackTrace();
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

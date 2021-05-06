package components;

import utilities.Utilities;
import utilities.VideoFile;
import utilities.VideoFileHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Consumer {
	
	private String IP, brokerIP = null;
	private String channelName; // whether user is registered or not. won't be used yet
	Socket consSocket = null;
	ObjectOutputStream consOutputStream = null;
	ObjectInputStream consInputStream = null;
	private int port, brokerPort = 0;
	VideoFile takenVideo;
	ArrayList<Integer> relativeVideosPerBroker;
	// broker port is the port on the Broker, which communicates with the Consumers
	
	/**
	 * There will probably be more properties
	 *
	 * @param IP   Consumer's ip address
	 * @param port Consumer's port number
	 */
	public Consumer(String IP, int port, String channelName) {
		this.IP = IP;
		this.port = port;
		this.channelName = channelName;
		relativeVideosPerBroker = new ArrayList<>();
	}
	
	public boolean findBroker(ArrayList<Broker> brokers, String creator) {
		if (brokers == null || brokers.isEmpty()) {
			return false;
		}
		BigInteger creatorsHash = Utilities.hash(creator);
		boolean found = false;
		for (Broker broker : brokers) {
			if (creatorsHash.compareTo(broker.getHashValue()) < 0) {
				this.brokerIP = broker.getIp();
				this.brokerPort = broker.getPortToConsumers();
				found = true;
				break;
			}
		}
		if (!found) {
			this.brokerIP = brokers.get(0).getIp();
			this.brokerPort = brokers.get(0).getPortToConsumers();
		}
		return true;
	}
	
	public boolean getByChannel(String creator) {
		try {
			consSocket = new Socket(brokerIP, brokerPort); // connects with broker to announce
			// existance
			
			consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
			consInputStream = new ObjectInputStream(consSocket.getInputStream());
			
			String searchedWord = "in:" + creator;
			
			consOutputStream.writeUTF(searchedWord); // consumer sends the searched word
			consOutputStream.flush();
			boolean foundFinalPiece = false;
			ArrayList<VideoFile> chosenVid = new ArrayList<VideoFile>();
			while (!foundFinalPiece) {
				try {//here we get the video from the broker with the *consInputStream*
					VideoFile current = (VideoFile) consInputStream.readObject();
					chosenVid.add(current);
					foundFinalPiece = current.isFinal();
				} catch (ClassNotFoundException e) {
					System.err.println("Problem with getting the video chunks");
					return false;
				}
			}
			consInputStream.close();
			consOutputStream.close();
			consSocket.close();
			if (chosenVid.get(0).getName().equals("EMPTY")) {
				return false;
			}
			takenVideo = VideoFileHandler.merge(chosenVid);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean getByHashtag(String hashtag) {
		try {
			consSocket = new Socket(brokerIP, brokerPort);
			consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
			consInputStream = new ObjectInputStream(consSocket.getInputStream());
			
			String searchedWord = "in:" + hashtag;
			String byWho = "by:" + (channelName == null ? "" : channelName);
			
			consOutputStream.writeUTF(searchedWord); // consumer sends the searched word
			consOutputStream.writeUTF(byWho); // consumer sends his name so that Broker doesn't send his own videos back to the consumer
			consOutputStream.flush();
			
			String code = consInputStream.readUTF();
			if (code.equals("VIDEO")) {
				boolean foundFinalPiece = false;
				ArrayList<VideoFile> chosenVid = new ArrayList<VideoFile>();
				while (!foundFinalPiece) {
					try {//here we get the video from the broker with the *consInputStream*
						VideoFile current = (VideoFile) consInputStream.readObject();
						chosenVid.add(current);
						foundFinalPiece = current.isFinal();
					} catch (ClassNotFoundException e) {
						System.err.println("Problem with getting the video chunks");
					}
				}
				takenVideo = VideoFileHandler.merge(chosenVid);
			} else if (code.equals("NOT FOUND")) {
				return false;
			} else {
				Broker tmp = Utilities.toBroker(code);
				consSocket = new Socket(tmp.getIp(), tmp.getPortToConsumers());
				consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
				consInputStream = new ObjectInputStream(consSocket.getInputStream());
				
				consOutputStream.writeUTF(searchedWord); // consumer sends the searched word
				consOutputStream.writeUTF(byWho); // consumer sends his name so that Broker doesn't send his own videos back to the consumer
				consOutputStream.flush();
				
				code = consInputStream.readUTF();
				if (!code.equals("VIDEO")) {
					return false;
				}
				boolean foundFinalPiece = false;
				ArrayList<VideoFile> chosenVid = new ArrayList<VideoFile>();
				while (!foundFinalPiece) {
					try {//here we get the video from the broker with the *consInputStream*
						VideoFile current = (VideoFile) consInputStream.readObject();
						chosenVid.add(current);
						foundFinalPiece = current.isFinal();
					} catch (ClassNotFoundException e) {
						System.err.println("Problem with getting the video chunks");
					}
				}
				takenVideo = VideoFileHandler.merge(chosenVid);
			}
			
			consInputStream.close();
			consOutputStream.close();
			consSocket.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void setRandomBroker(ArrayList<Broker> brokers) {
		int i = new Random().nextInt(brokers.size());
		this.brokerIP = brokers.get(i).getIp();
		this.brokerPort = brokers.get(i).getPortToConsumers();
	}
	
	private class ToBrokerThread extends Thread {
	
	}
	
	public void writeVideoFile(String folderName) {
		VideoFileHandler.writeFile(takenVideo, folderName);
	}
	
	public String getIP() {
		return IP;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getChannelName() {
		return channelName;
	}
}
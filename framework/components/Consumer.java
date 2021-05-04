package components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

import utilities.Utilities;
import utilities.VideoFile;
import utilities.VideoFileHandler;

public class Consumer {
	
	private String IP, brokerIP = null;
	private String channelName; // whether user is registered or not. won't be used yet
	Socket consSocket = null;
	ObjectOutputStream consOutputStream = null;
	ObjectInputStream consInputStream = null;
	private int port, brokerPort = 0;
	VideoFile takenVideo;
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
	
	public boolean getByChannel(String creator){
		// TODO: send creator to Broker (we know which, see brokerIP, brokerPort)
		// TODO: and get a List of VideoFiles, merge them (into takenVideo) and return true
		//  or get a VideoFile with name "EMPTY" and return false
		//  (if no videos from this channel are available)
	}
	
	public void writeVideoFile() {
		VideoFileHandler.writeFile(takenVideo, "Consumer" + IP + port)
	}
	
	public void connectCons(String inputWord) {
		try {
			consSocket = new Socket(brokerIP, brokerPort); // connects with broker to announce
			// existance
			
			consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
			consInputStream = new ObjectInputStream(consSocket.getInputStream());
			
			String searchedWord = "in:" + inputWord;
			String byWho = "by:" + (channelName == null ? "" : channelName);
			
			consOutputStream.writeObject(searchedWord); // consumer sends the searched word
			consOutputStream.writeObject(byWho); // consumer sends his name so that Broker doesn't send his own videos back to the consumer
			consOutputStream.flush();
			
			//edw mesa milaei me ton broker gia na kanei to init mallon
			
			
			boolean foundFinalPiece = false;
			ArrayList<VideoFile> chosenVid = null;
			while (!foundFinalPiece) {
				try {
					chosenVid.add((VideoFile) consInputStream.readObject()); //den eimai sigoyros an tha ginei me ayto ton tropo
				} catch (ClassNotFoundException e) {
					System.err.println("Problem with getting the video chunks");
				}
			} //here we get the video from the broker with the *consInputStream*
			
			
			//TODO: we use the list to download the video with maiks methods
			
			consInputStream.close();
			consOutputStream.close();
			consSocket.close();
			// return true;
		} catch (IOException e) {
			e.printStackTrace();
			// return false;
		}
		
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
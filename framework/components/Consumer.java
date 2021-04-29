package components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import utilities.Utilities;

public class Consumer {

	private final String IP;
	// broker port is the port on the Broker, which communicates with the Consumers
//	private boolean isLoggedIn; // whether user is registered or not. won't be used yet
	Socket consSocket = null;
	ObjectOutputStream consOutputStream = null;
	ObjectInputStream consInputStream = null;
	private final int port, brokerPort;

	/**
	 * There will probably be more properties
	 *
	 * @param IP   Consumer's ip address
	 * @param port Consumer's port number
	 */
	public Consumer(String IP, int port) {
//		this.isLoggedIn = false;
		this.IP = IP;
		this.port = port;
		this.brokerPort = Utilities.BROKER_PORT_TO_CON;
	}

	public void connectCons(String inputWord) {
		try {
			consSocket = new Socket(InetAddress.getByName("127.0.0.1"), brokerPort); // connects with broker to announce
																						// existance

			consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
			consInputStream = new ObjectInputStream(consSocket.getInputStream());

			consOutputStream.writeObject(inputWord); // consumer sends the searched word
			consOutputStream.flush();

			// TODO: here we get the video from the broker with the *consInputStream*

			consInputStream.close();
			consOutputStream.close();
			consSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String addHashtag(String inputWord) { // we will call this when the user inputs a hashtag
		inputWord = "#" + inputWord;
		return inputWord;

	}

	public String getIP() {
		return IP;
	}

	public int getPort() {
		return port;
	}

//	public boolean isLoggedIn() {
//		return isLoggedIn;
//	}
}
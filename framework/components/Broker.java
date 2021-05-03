package components;

import utilities.Utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class Broker implements Comparable<Broker> {

	private final int portToPublishers, portToConsumers;
	private final String ip;
	ServerSocket brokerSocket;
	Socket connection = null;
	BigInteger hashValue = null;
	
	/**
	 *
	 * @param ip
	 * @param portToPublishers
	 * @param portToConsumers
	 */
	public Broker(String ip, int portToPublishers, int portToConsumers) {
		this.ip = ip;
		this.portToPublishers = portToPublishers;
		this.portToConsumers = portToConsumers;
		this.hashValue = Utilities.hash(this.ip, this.portToPublishers);
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
	
	public void openBroker() {

		try {

			brokerSocket = new ServerSocket(); // one for clients one for publishers

			while (true) {
				connection = brokerSocket.accept();

				Thread t = new ClientHandler(connection);

				t.start();

			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				brokerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}
	
	@Override
	public int compareTo(Broker o) {
		return this.hashValue.compareTo(o.getHashValue());
	}
}

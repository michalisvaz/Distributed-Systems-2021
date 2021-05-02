package components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Broker {

	private final int portToPublishers, portToConsumers;
	private final String ip;
	ServerSocket brokerSocket;
	Socket connection = null;
	
	
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

}

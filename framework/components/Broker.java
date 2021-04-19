package components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Broker {

    private final int port;
    ServerSocket brokerSocket;
    Socket connection = null;

    /**
     * There will be many more properties
     *
     * @param port broker's ip address
     */

    public Broker(int port) {
        this.port = port;
    }


    public int getPort() {
        return port;
    }

    public void openBroker(){

        try {

            brokerSocket = new ServerSocket(port);

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

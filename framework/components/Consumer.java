package components;

import utilities.Utilities;

public class Consumer {

    private final String IP;
    // broker port is the port on the Broker, which communicates with the Consumers
    private final int port, brokerPort;
    private boolean isLoggedIn; // whether user is registered or not

    /**
     * There will probably be more properties
     *
     * @param IP Consumer's ip address
     * @param port Consumer's port number
     */
    public Consumer(String IP, int port) {
        this.isLoggedIn = false;
        this.IP = IP;
        this.port = port;
        this.brokerPort = Utilities.BROKER_PORT_TO_CON;
    }

    public String getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
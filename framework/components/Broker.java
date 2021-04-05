package components;

public class Broker {

    private final String IP;

    /**
     * There will be many more properties
     *
     * @param IP broker's ip address
     */
    public Broker(String IP) {
        this.IP = IP;
    }

    public String getIP() {
        return IP;
    }

}

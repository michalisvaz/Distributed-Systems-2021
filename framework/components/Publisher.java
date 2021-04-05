package components;

import utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

public class Publisher {

    private final String IP;
    private String channelName;
    private List<String> hashtags;
    private final int port, brokerPort;

    /**
     * Maybe more properties will be needed
     *
     * @param IP ip address of Publisher
     * @param channelName name of Publisher's channel
     * @param port port number of the Publisher
     * @param hashtags the hashtags for which Publisher has at least one video
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

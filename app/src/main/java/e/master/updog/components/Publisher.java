package e.master.updog.components;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import e.master.updog.utilities.Utilities;
import e.master.updog.utilities.VideoFile;
import e.master.updog.utilities.VideoFileHandler;

public class Publisher {

    private String IP, brokerIP = null;
    private String channelName;
    Socket pubSocket = null;
    ObjectOutputStream pubOutputStream = null;
    private int port, brokerPort = 0;
    private VideoFile currentVideo = null;

    /**
     * Constructor
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

    /**
     * sets brokerIP and brokerPort using hash values. This combination of Ip and port are used so that the
     * publisher knows which broker to upload their videos to.
     *
     * @param brokers list of brokers. Assume it is sorted
     * @return true if everything went ok, false otherwise
     */
    public boolean init(ArrayList<Broker> brokers) {
        if (brokers == null || brokers.isEmpty()) {
            return false;
        }
        // hash the producer's channel name
        BigInteger myHash = Utilities.hash(channelName);
        // iterate the (sorted) list of brokers. If producer's hash is less than the broker's hashValue,
        // then we found the broker. Note that by breaking the loop once we found a broker with greater hashValue
        // than our own (and because the list is sorted), we end up with matching the producer to the first broker
        // with a greater hash than ours.
        boolean found = false;
        for (Broker broker : brokers) {
            if (myHash.compareTo(broker.getHashValue()) < 0) {
                this.brokerIP = broker.getIp();
                this.brokerPort = broker.getPortToPublishers();
                found = true;
                break;
            }
        }
        // if we didn't find a broker, then our hashValue is greater than all the brokers' hashValues
        // so we are matched to the first broker
        if (!found) {
            this.brokerIP = brokers.get(0).getIp();
            this.brokerPort = brokers.get(0).getPortToPublishers();
        }
        return true;
    }

    /**
     * Reads from the user a name corresponding to the file which should be uploaded (or CANCEL if the user regrets it)
     * And then reads the file from the disk. This was only used in the first part of the project.
     */
    public void readFile() {
        currentVideo = null;
        System.out.println("Give the name of the file you wish to upload");
        Scanner sc = new Scanner(System.in);
        String videoName = sc.nextLine();
        if (videoName.equals("CANCEL")) {
            currentVideo = null;
            return;
        }
        currentVideo = VideoFileHandler.readFile(videoName, channelName);
        while (currentVideo == null) {
            System.out.println("Video no found. Try again. Type CANCEL to cancel your video upload.");
            sc = new Scanner(System.in);
            videoName = sc.nextLine();
            if (videoName.equals("CANCEL")) {
                currentVideo = null;
                return;
            }
            currentVideo = VideoFileHandler.readFile(videoName, channelName);
        }
    }

    /**
     * Send (upload) the file to the Broker
     *
     * @return true if everything went ok, false if there were problems
     */
    public boolean push() {

        try {
            // Create socket and stream
            pubSocket = new Socket(brokerIP, brokerPort);
            pubOutputStream = new ObjectOutputStream(pubSocket.getOutputStream());
            pubOutputStream.writeBoolean(true); // this means "I am going to send you a video (chunk by chunk)"
            pubOutputStream.flush();
            // Split the video to chunks and send it
            ArrayList<VideoFile> result = VideoFileHandler.split(currentVideo);
            for (VideoFile x : result) {
                pubOutputStream.writeObject(x);
                pubOutputStream.flush();
            }
            // close socket and stream
            pubOutputStream.close();
            pubSocket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    public void setCurrentVideo(VideoFile currentVideo) {
        this.currentVideo = currentVideo;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

}

package e.master.updog.components;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import e.master.updog.utilities.Pair;
import e.master.updog.utilities.Utilities;
import e.master.updog.utilities.VideoFile;
import e.master.updog.utilities.VideoFileHandler;

public class Broker implements Comparable<Broker> {

    private final int portToPublishers, portToConsumers;
    private final String ip;
    ServerSocket brokerSocketToPublishers, brokerSocketToConsumers;
    Socket connection = null;
    BigInteger hashValue;
    private Vector<ToPublisherThread> publishers;
    private Vector<ToConsumerThread> consumers;
    private HashMap<String, VideoFile> videoFiles;
    private HashMap<String, ArrayList<Pair>> brokersToHashtags;
    private ArrayList<Broker> otherBrokers;

    /**
     * @param ip
     * @param portToPublishers
     * @param portToConsumers
     */
    public Broker(String ip, int portToPublishers, int portToConsumers) {
        this.ip = ip;
        this.portToPublishers = portToPublishers;
        this.portToConsumers = portToConsumers;
        this.hashValue = Utilities.hash(this.ip, this.portToPublishers);
        this.publishers = new Vector<>();
        this.consumers = new Vector<>();
        this.videoFiles = new HashMap<>();
        this.brokersToHashtags = new HashMap<>();
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

    public ServerSocket getBrokerSocketToPublishers() {
        return brokerSocketToPublishers;
    }

    public ServerSocket getBrokerSocketToConsumers() {
        return brokerSocketToConsumers;
    }

    public Socket getConnection() {
        return connection;
    }

    public Vector<ToPublisherThread> getPublishers() {
        return publishers;
    }

    public HashMap<String, VideoFile> getVideoFiles() {
        return videoFiles;
    }

    /**
     * Initialize the list of other existing brokers and the HashMap about which Broker has locally
     * videos for which (channel, hashtag) combinations (this is used to do redirection correctly, see class
     * Pair for more info)
     * @param otherBrokers an ArrayList with the other existing brokers
     */
    public void setOtherBrokers(ArrayList<Broker> otherBrokers) {
        this.otherBrokers = new ArrayList<>();
        for (Broker b : otherBrokers) {
            Broker tmp = new Broker(b.getIp(), b.getPortToPublishers(), b.getPortToConsumers());
            this.otherBrokers.add(tmp);
            this.brokersToHashtags.put(b.getString(), new ArrayList<>());
        }
    }

    /**
     * Runs a method to connect with the Publishers and receive data from them, and a method to send data to users
     * This methods also talk with other brokers when necessary, that is when they need to redirect a query or to
     * update the brokersToHashtags data structure
     */
    public void runBroker() {
        System.out.println("Running Broker with ip " + ip + " and ports " + portToPublishers + ", " + portToConsumers);
        System.out.println("Broker's hash value: " + hashValue);
        receiveData();
        sendData();
    }

    /**
     * Creates and Runs a new Data-receiving Thread (ToPublisherThread)
     */
    private void receiveData() {
        new Thread("Data-receiving Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        InetAddress addr = InetAddress.getByName(ip);
                        brokerSocketToPublishers = new ServerSocket(portToPublishers, 50, addr);
                        Socket socket = brokerSocketToPublishers.accept();
                        ToPublisherThread newPublisher = new ToPublisherThread(socket);
                        newPublisher.start();
                        synchronized (publishers) {
                            publishers.addElement(newPublisher);
                        }
                        brokerSocketToPublishers.close();
                    } catch (IOException e) {
                        System.out.println("Failed to establish publisher-broker connection");
                        // e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * The job of these threads is to receive videos from Publishers and to communicate with other brokers
     * to update the brokersToHashtags data structure accordingly each time a new video is added
     */
    private class ToPublisherThread extends Thread {
        public Socket socket;
        private ObjectInputStream ois;

        public ToPublisherThread(Socket socket) {
            this.socket = socket;
            try {
                InputStream is = socket.getInputStream();
                ois = new ObjectInputStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                boolean isVideoFile = ois.readBoolean();
                if (isVideoFile) { // receive a new video from a Publisher
                    ArrayList<VideoFile> chunks = new ArrayList<VideoFile>();
                    boolean endFound = false;
                    System.out.println("Getting files");
                    // Get the VideoFile chunk by chunk
                    while (!endFound) {
                        VideoFile chunk = (VideoFile) ois.readObject();
                        chunks.add(chunk);
                        endFound = chunk.isFinal();
                    }
                    // merge it
                    VideoFile res = VideoFileHandler.merge(chunks);
                    // put it in the list of your videoFiles (don't keep the video byte array in memory)
                    synchronized (videoFiles) {
                        VideoFileHandler.writeFile(res, System.getProperty("user.dir") + "/Broker" + getIp()
                                + getPortToPublishers() + getPortToConsumers());
                        res.setData(null);
                        videoFiles.put(res.getName(), res);
                    }
                    System.out.println("Successfully merged and saved file");
                    Pair toPut;
                    // update your local brokersToHashtags
                    synchronized (brokersToHashtags) {
                        for (String htg : res.getHashtags()) {
                            toPut = new Pair(htg, res.getChannel());
                            brokersToHashtags.get(getString()).add(toPut);
                        }
                    }
                    // send the (updated) brokersToHashtags to other brokers
                    for (Broker br : otherBrokers) {
                        if (br.getString().equals(getString())) {
                            continue;
                        }
                        String ipToSpeak = br.getIp();
                        int portToSpeak = br.getPortToPublishers();
                        Socket tmpSocket = new Socket(ipToSpeak, portToSpeak);
                        OutputStream tmpOutStream = tmpSocket.getOutputStream();
                        ObjectOutputStream tmpObjectOutStream = new ObjectOutputStream(tmpOutStream);
                        tmpObjectOutStream.writeBoolean(false);
                        tmpObjectOutStream.flush();
                        tmpObjectOutStream.writeObject(brokersToHashtags);
                        tmpObjectOutStream.flush();
                        tmpSocket.close();
                        tmpOutStream.close();
                        tmpObjectOutStream.close();
                    }
                } else { // receive an updated brokersToHashtags object from another broker
                    Object tmpObject = ois.readObject();
                    if (tmpObject == null) {
                        System.out.println("Something went wrong updating brokers to hashtags HashMap");
                    } else {
                        brokersToHashtags = (HashMap<String, ArrayList<Pair>>) tmpObject;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates and Runs a new Data-sending Thread (ToConsumerThread)
     */
    public void sendData() {
        new Thread("Data-sending Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        InetAddress addr = InetAddress.getByName(ip);
                        brokerSocketToConsumers = new ServerSocket(portToConsumers, 50, addr);
                        Socket socket = brokerSocketToConsumers.accept();
                        ToConsumerThread newConsumer = new ToConsumerThread(socket);
                        newConsumer.start();
                        synchronized (consumers) {
                            consumers.addElement(newConsumer);
                        }
                        brokerSocketToConsumers.close();
                    } catch (IOException e) {
                        System.out.println("Failed to establish consumer-broker connection");
                        // e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * The job of these threads is to talk with the Consumers when a video of a certain creator
     * or hashtag is requested. These threads may also need to send a list of all the brokers to a
     * newly connected appNode
     */
    private class ToConsumerThread extends Thread {
        public Socket socket;
        private ObjectInputStream oins;
        private ObjectOutputStream oouts;

        public ToConsumerThread(Socket socket) {
            this.socket = socket;
            try {
                InputStream is = socket.getInputStream();
                oins = new ObjectInputStream(is);
                OutputStream os = socket.getOutputStream();
                oouts = new ObjectOutputStream(os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String searchedWord = oins.readUTF();
                if (searchedWord.equals("GETBROKERLIST")) { // if a newly-connected AppNode wants to know the brokers
                    for (Broker x : otherBrokers) {
                        oouts.writeUTF(x.getString());
                        oouts.flush();
                    }
                    oouts.writeUTF("FINISHED");
                    oouts.flush();
                } else if (searchedWord.charAt(3) == '#') { // if user searched by hashtag
                    String byWho = oins.readUTF();
                    // count how many videos not belonging to client, you have with this hashtag
                    int cnt = 0;
                    String hashtag = searchedWord.replace("#", "").replace("in:", "").toLowerCase().trim();
                    String clientName = byWho.replace("by:", "").trim();
                    for (VideoFile vf : videoFiles.values()) {
                        if (vf.getHashtags().contains(hashtag) && !vf.getChannel().trim().equals(clientName)) {
                            cnt += 1;
                        }
                    }
                    // if you have at least one, send one of them (randomly)
                    if (cnt > 0) {
                        oouts.writeUTF("VIDEO");
                        oouts.flush();
                        // A random integer in [0, cnt-1]. This is the "count" of the video to send
                        int indexToSend = new Random().nextInt(cnt);
                        VideoFile toSend = null;
                        for (VideoFile vf : videoFiles.values()) {
                            if (vf.getHashtags().contains(hashtag) && !vf.getChannel().trim().equals(clientName)) {
                                if (indexToSend == 0) {
                                    toSend = vf;
                                    break;
                                } else {
                                    indexToSend -= 1;
                                }
                            }
                        }
                        // read video from the correct place
                        String directory = System.getProperty("user.dir") + "/Broker" + ip + portToPublishers
                                + portToConsumers + "/";
                        toSend = VideoFileHandler.readFile(directory + toSend.getName(), toSend.getChannel());
                        // and send it
                        ArrayList<VideoFile> result = VideoFileHandler.split(toSend);
                        for (VideoFile x : result) {
                            oouts.writeObject(x);
                            oouts.flush();
                        }
                    } else { // else check whether any other broker has such a video
                        String where = null;
                        outer:
                        for (String key : brokersToHashtags.keySet()) {
                            ArrayList<Pair> pairs = brokersToHashtags.get(key);
                            for (Pair p : pairs) {
                                if (p.getHashtag().equals(hashtag) && !p.getChannel().equals(clientName)) {
                                    where = key;
                                    break outer;
                                }
                            }
                        }
                        // if there exists a video with the wanted hashtag in another broker,
                        // send the brokers info to the client
                        if (where != null) {
                            oouts.writeUTF(where);
                            oouts.flush();
                        } else { // else send that the video was not found
                            oouts.writeUTF("NOT FOUND");
                            oouts.flush();
                        }
                    }
                } else { //if user searched by channel
                    // candidate videos for sending
                    ArrayList<VideoFile> maybeToSend = new ArrayList<>();
                    String requestedChannel = searchedWord.replace("in:", "").toLowerCase();
                    for (VideoFile vf : videoFiles.values()) {
                        if (vf.getChannel().trim().equalsIgnoreCase(requestedChannel.trim())) {
                            maybeToSend.add(vf);
                        }
                    }
                    // Here there is no chance of having videos from the requested channel in another broker. So:
                    // if there are no videos by specified channel
                    if (maybeToSend.isEmpty()) {
                        VideoFile toSend = new VideoFile("EMPTY", null, new ArrayList<>(), 0, true);
                        oouts.writeObject(toSend);
                        oouts.flush();
                    } else { // if we have videos to send choose randomly one
                        int randIndex = new Random().nextInt(maybeToSend.size());
                        VideoFile toSend = maybeToSend.get(randIndex);
                        // read from the right place
                        String directory = System.getProperty("user.dir") + "/Broker" + ip + portToPublishers
                                + portToConsumers + "/";
                        toSend = VideoFileHandler.readFile(directory + toSend.getName(), requestedChannel.trim());
                        // and send it
                        ArrayList<VideoFile> result = VideoFileHandler.split(toSend);
                        for (VideoFile x : result) {
                            oouts.writeObject(x);
                            oouts.flush();
                        }
                    }
                }
                oouts.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Compare to another Broker
     * @param o the other Broker
     * @return true if their hashValues are the same, false otherwise
     */
    @Override
    public int compareTo(Broker o) {
        return this.hashValue.compareTo(o.getHashValue());
    }

    /**
     *
     * @return a string representation of the broker
     */
    public String getString() {
        return this.getIp() + ";" + this.getPortToPublishers() + ";" + this.getPortToConsumers();
    }

}

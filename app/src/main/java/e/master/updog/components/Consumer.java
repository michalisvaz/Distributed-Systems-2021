package e.master.updog.components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import e.master.updog.utilities.Utilities;
import e.master.updog.utilities.VideoFile;
import e.master.updog.utilities.VideoFileHandler;

public class Consumer {

    private String IP, brokerIP = null;
    private String channelName;
    Socket consSocket = null;
    ObjectOutputStream consOutputStream = null;
    ObjectInputStream consInputStream = null;
    private int port, brokerPort = 0;
    VideoFile takenVideo;
    ArrayList<Integer> relativeVideosPerBroker;

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
        relativeVideosPerBroker = new ArrayList<>();
    }

    /**
     * Takes a list of all the brokers available and a channel name, finds which broker is
     * responsible for this channel name and sets the variables brokerIP and brokerPort to this broker's
     * information. Finally it returns true if it found a broker responsible for the channel name specified
     * and false otherwise.
     * @param brokers a list of all the existing brokers of our system
     * @param creator the channel name to check which broker is responsible for it
     * @return
     */
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
        // if the hash value of the channel name was greater than all the other broker's hash values,
        // then the channel name is matched to the first broker (because we have a clock-like numbering)
        if (!found) {
            this.brokerIP = brokers.get(0).getIp();
            this.brokerPort = brokers.get(0).getPortToConsumers();
        }
        return true;
    }

    /**
     * Send the broker a request specifying a channel from which the Consumer wants a video
     * @param creator the channel from which the Consumer wants a video
     * @return true if you successfully received an actual video, false otherwise
     */
    public boolean getByChannel(String creator) {
        try {
            // create necessary sockets and streams
            consSocket = new Socket(brokerIP, brokerPort);
            consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
            consInputStream = new ObjectInputStream(consSocket.getInputStream());

            // create and send your message
            String searchedWord = "in:" + creator;
            consOutputStream.writeUTF(searchedWord); // consumer sends the searched word
            consOutputStream.flush();

            // get VideoFile chunk by chunk and store it in ArrayList.
            // Receive data until final piece is found.
            boolean foundFinalPiece = false;
            ArrayList<VideoFile> chosenVid = new ArrayList<VideoFile>();
            while (!foundFinalPiece) {
                try {//here we get the video from the broker with the *consInputStream*
                    VideoFile current = (VideoFile) consInputStream.readObject();
                    chosenVid.add(current);
                    foundFinalPiece = current.isFinal();
                } catch (ClassNotFoundException e) {
                    System.err.println("Problem with getting the video chunks");
                    return false;
                }
            }
            // close socket and streams
            consInputStream.close();
            consOutputStream.close();
            consSocket.close();
            // if no actual video was found
            if (chosenVid.get(0).getName().equals("EMPTY")) {
                return false;
            }
            // merge the chunks into an actual video
            takenVideo = VideoFileHandler.merge(chosenVid);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send the broker a request specifying a hashtag from which the Consumer wants a video
     * @param hashtag the hashtag from which the Consumer wants a video
     * @return true if you successfully received an actual video, false otherwise
     */
    public boolean getByHashtag(String hashtag) {
        try {
            // create sockets and streams
            consSocket = new Socket(brokerIP, brokerPort);
            consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
            consInputStream = new ObjectInputStream(consSocket.getInputStream());

            // We need both the hashtag and who is searching it to avoid sending to a Consumer their own videos
            String searchedWord = "in:" + hashtag;
            String byWho = "by:" + (channelName == null ? "" : channelName);
            consOutputStream.writeUTF(searchedWord); // consumer sends the searched word
            consOutputStream.writeUTF(byWho); // consumer sends his name so that Broker doesn't send his own videos back to the consumer
            consOutputStream.flush();

            // read a code
            // If code is equal to "VIDEO", you're going to receive a video chunk by chunk
            // If code is equal to "NOT FOUND", there isn't such a hashtag in the system (or there exists but it
            // belongs to the consumer asking for it)
            // Otherwise, the consumer will get a response but from a different broker
            String code = consInputStream.readUTF();
            if (code.equals("VIDEO")) {
                boolean foundFinalPiece = false;
                ArrayList<VideoFile> chosenVid = new ArrayList<VideoFile>();
                while (!foundFinalPiece) {
                    try {//here we get the video from the broker with the *consInputStream*
                        VideoFile current = (VideoFile) consInputStream.readObject();
                        chosenVid.add(current);
                        foundFinalPiece = current.isFinal();
                    } catch (ClassNotFoundException e) {
                        System.err.println("Problem with getting the video chunks");
                        return false;
                    }
                }
                takenVideo = VideoFileHandler.merge(chosenVid);
            } else if (code.equals("NOT FOUND")) {
                return false;
            } else {
                // create sockets and streams to another broker
                Broker tmp = Utilities.toBroker(code);
                consSocket = new Socket(tmp.getIp(), tmp.getPortToConsumers());
                consOutputStream = new ObjectOutputStream(consSocket.getOutputStream());
                consInputStream = new ObjectInputStream(consSocket.getInputStream());

                // send them your request
                consOutputStream.writeUTF(searchedWord); // consumer sends the searched word
                consOutputStream.writeUTF(byWho); // consumer sends his name so that Broker doesn't send his own videos back to the consumer
                consOutputStream.flush();

                // if the response is anything else except a video, return false
                code = consInputStream.readUTF();
                if (!code.equals("VIDEO")) {
                    return false;
                }
                // otherwise get the videoFile as you normally would
                boolean foundFinalPiece = false;
                ArrayList<VideoFile> chosenVid = new ArrayList<VideoFile>();
                while (!foundFinalPiece) {
                    try {//here we get the video from the broker with the *consInputStream*
                        VideoFile current = (VideoFile) consInputStream.readObject();
                        chosenVid.add(current);
                        foundFinalPiece = current.isFinal();
                    } catch (ClassNotFoundException e) {
                        System.err.println("Problem with getting the video chunks");
                    }
                }
                takenVideo = VideoFileHandler.merge(chosenVid);
            }
            // close the socket and the streams
            consInputStream.close();
            consOutputStream.close();
            consSocket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Choose a random broker for the consumer to talk to
     * @param brokers the brokers from which to choose
     */
    public void setRandomBroker(ArrayList<Broker> brokers) {
        int i = new Random().nextInt(brokers.size());
        this.brokerIP = brokers.get(i).getIp();
        this.brokerPort = brokers.get(i).getPortToConsumers();
    }

    private class ToBrokerThread extends Thread {

    }

    public VideoFile getTakenVideo() {
        return takenVideo;
    }

    public void writeVideoFile(String folderName) {
        VideoFileHandler.writeFile(takenVideo, folderName);
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
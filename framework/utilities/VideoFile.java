package utilities;

import java.util.ArrayList;
import java.util.List;

public class VideoFile {

    private String name, channel;
    private List<String> hashtags;

    /**
     * There will be (many) more properties/variables for this object
     *
     * @param name full name of the file (it may contain channel and hashtags)
     * @param channel channel of the video's owner
     * @param hashtags list of hashtags the video belongs to
     */
    public VideoFile(String name, String channel, List<String> hashtags){
        this.name = name;
        this.channel = channel;
        this.hashtags = new ArrayList<String>();
        for(String h:hashtags){
            this.hashtags.add(h);
        }
    }

    public String getName() {
        return name;
    }

    public String getChannel() {
        return channel;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

}

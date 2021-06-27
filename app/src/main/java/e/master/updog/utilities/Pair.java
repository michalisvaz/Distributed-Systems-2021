package e.master.updog.utilities;

import java.io.Serializable;
import java.util.Objects;

/**
 * We use this class to keep track of which combinations (Channel, Hashtag) is responsible each Broker.
 * When a Consumer asks Broker X1 if they have a video with hashtag #yyyy, and X1 doesn't have such a video,
 * then X1 must redirect the query to another Broker XX. To be sure that XX has a video of hashtag yyyy **that
 * is not uploaded by the current client**, we need to keep track only of the hashtags for which each broker
 * is responsible but of the combinations (channel, hashtag).
 */
public class Pair implements Serializable {

    private String hashtag, channel;

    public Pair(String hashtag, String channel) {
        this.hashtag = hashtag;
        this.channel = channel;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return hashtag.equals(pair.hashtag) && channel.equals(pair.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashtag, channel);
    }
}

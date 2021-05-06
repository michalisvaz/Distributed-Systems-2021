package utilities;

import java.io.Serializable;
import java.util.Objects;

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
	
	public boolean equals(Pair o) {
		return hashtag.equals(o.hashtag) && channel.equals(o.channel);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(hashtag, channel);
	}
}

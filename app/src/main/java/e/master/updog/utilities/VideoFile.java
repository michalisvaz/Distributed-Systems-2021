package e.master.updog.utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoFile implements Serializable {
	
	private String name, channel;
	private List<String> hashtags;
	private long size;
	private byte[] data;
	private boolean isFinal;
	
	/**
	 * There will be (many) properties/variables for this object
	 *
	 * @param name     full name of the file (it may contain channel and hashtags)
	 * @param channel  channel of the video's owner
	 * @param hashtags list of hashtags the video belongs to
	 * @param size     the size of the videofile
	 * @param isFinal  whether the VideoFile is the final chunk of a larger(?) VideoFile or not
	 */
	public VideoFile(String name, String channel, List<String> hashtags, long size, boolean isFinal) {
		this.name = name;
		this.channel = channel;
		this.hashtags = new ArrayList<String>();
		for (String h : hashtags) {
			this.hashtags.add(h);
		}
		this.size = size;
		this.isFinal = isFinal;
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
	
	public long getSize() {
		return size;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public boolean isFinal() {
		return isFinal;
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
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public void setFinal(boolean aFinal) {
		isFinal = aFinal;
	}
	
	/**
	 * @return a prettier String representation of the VideoFile
	 */
	public String toString() {
		String res = "Name: ";
		res += this.name;
		res += "\n";
		res += "Channel: ";
		res += this.channel;
		res += "\n";
		res += "Hashtags: ";
		res += String.join(", ", this.hashtags);
		res += "\n";
		res += "Size: ";
		res += this.size;
		res += "\n";
		res += "Is Final: ";
		res += this.isFinal;
		res += "\n";
		return res;
	}
	
}

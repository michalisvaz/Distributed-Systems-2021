package utilities;

import java.util.ArrayList;
import java.util.List;

public class VideoFile {
	
	private String name, channel;
	private List<String> hashtags;
	private long size;
	private byte[] data;
	
	/**
	 * There will be (many) more properties/variables for this object
	 *
	 * @param name     full name of the file (it may contain channel and hashtags)
	 * @param channel  channel of the video's owner
	 * @param hashtags list of hashtags the video belongs to
	 * @param size     the size of the videofile
	 */
	public VideoFile(String name, String channel, List<String> hashtags, long size) {
		this.name = name;
		this.channel = channel;
		this.hashtags = new ArrayList<String>();
		for (String h : hashtags) {
			this.hashtags.add(h);
		}
		this.size = size;
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
	
	public String toString(){
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
		return res;
	}
	
}

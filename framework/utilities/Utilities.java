package utilities;

import components.Broker;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utilities {
	
	// Broker port used for communication with Publishers and with Consumers
	public static final String videoFolder = "../Videos";
	
	/**
	 * Hashes a String and returns the result as a BigInteger
	 *
	 * @param name input to hash
	 * @return result of hashing
	 */
	public static BigInteger hash(String name) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] result = md.digest(name.getBytes());
			return new BigInteger(result);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Hashes the result of the concatenation of a String and an integer. The String
	 * corresponds to an IP address and the integer to a port number.
	 *
	 * @param ip   a String corresponding to the IP address.
	 * @param port an integer corresponding to the port number.
	 * @return result of hashing
	 */
	public static BigInteger hash(String ip, int port) {
		String name = ip + port;
		return hash(name);
	}
	
	/**
	 * Used for synchronization of regular prints
	 *
	 * @param str the string to be print
	 */
	public static synchronized void print(String str) {
		System.out.println(str);
	}
	
	/**
	 * Used for synchronization of error prints
	 *
	 * @param str the string to be print
	 */
	public static synchronized void printError(String str) {
		System.err.println("ERROR: " + str);
	}
	
	public static void print(HashMap<String, VideoFile> map) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println("File name: " + pair.getKey() + "\n" + pair.getValue().toString());
			it.remove();
		}
	}
	
	/**
	 * Convert a string to a hashtag.
	 * For example dog is converted to #dog
	 * and #cat is converted to #cat
	 *
	 * @param inputWord the word to convert to a hashtag
	 * @return the word converted to hashtag
	 */
	public static String addHashtag(String inputWord) {
		inputWord = "#" + inputWord.replace("#", "");
		return inputWord;
	}
	
	/**
	 * Checks whether given string is valid info to initialize a broker
	 * @param line the string to check
	 * @return true if string contains a valid IP and valid ports
	 */
	public static boolean checkBrokerInfo(String line) {
		line = line.trim();
		String[] lparts = line.split(";");
		if (lparts.length != 3) {
			return false;
		}
		try {
			int p1 = Integer.parseInt(lparts[1]);
			int p2 = Integer.parseInt(lparts[1]);
			if (p1 < 1024 || p2 < 1024) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		String IP = lparts[0];
		String[] parts = IP.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		for (String part : parts) {
			try{
				int value = Integer.parseInt(part);
				if ((value < 0) || (value > 255)){
					return false;
				}
			}catch (NumberFormatException e){
				return false;
			}
		}
		if (IP.endsWith(".")){
			return false;
		}
		return true;
	}
	
	/**
	 * Converts a string representation of a Broker to Broker object
	 * @param str the string representation of the Broker
	 * @return the Broker which comes from that string
	 */
	public static Broker toBroker(String str){
		String[] parts = str.split(";");
		return new Broker(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
	}
	
}
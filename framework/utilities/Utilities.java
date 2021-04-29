package utilities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utilities {

	// Broker port used for communication with Publishers and with Consumers
	public static final int BROKER_PORT_TO_PUB = 7373;
	public static final int BROKER_PORT_TO_CON = 1999;
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

}
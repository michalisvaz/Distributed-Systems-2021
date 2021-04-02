import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilities{

    /**
     * Hashes a String and returns the result as a BigInteger
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
     * Hashes the result of the concatenation of a String and an integer. 
     * The String corresponds to an IP address and the integer to a port number.
     * @param ip a String corresponding to the IP address.
     * @param port an integer corresponding to the port number.
     * @return result of hashing
     */
    public static BigInteger hash(String ip, int port){
        String name = ip + port;
        return hash(name);
    }

    /**
     * Used for synchronization of regular prints
     * @param str the string to be print
     */
    public static synchronized void print(String str){
        System.out.println(str);
    }

    /**
     * Used for synchronization of error prints
     * @param str the string to be print
     */
    public static synchronized void printError(String str){
        System.err.println("ERROR: " + str);
    }

}
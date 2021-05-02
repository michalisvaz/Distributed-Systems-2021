
// import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import components.*;
import utilities.Utilities;

public class AppNodeMain {
	
	static Scanner sc = new Scanner(System.in);
	static Publisher publisher = null;
	static Consumer consumer = null;
	
	public static void main(String[] args) {
		
		String channelName = null;
		while (true) {
			int input = menu();
			switch (input) {
				case 1:
					if (channelName == null) {
						System.out.println("Add your channelName: ");
						channelName = sc.nextLine();
						while (channelName.startsWith("#")) {
							System.out.println("Channel name can't start with a hashtag. Try again: ");
							channelName = sc.nextLine();
						}
					}
					publisher = new Publisher(null, channelName, 0, null);
					// TODO: we should probably care about the IPs
					// and the ports to work from a distance *Pub*
					publisher.connectPub();
					publisher = null;
					break; // break the switch
				case 2:
					System.out.println("Creator's name: ");
					String creator = sc.nextLine();
					boolean flag1 = creator.startsWith("#");
					boolean flag2 = creator.equals(channelName);
					while (flag1 || flag2) {
						if (flag1) {
							System.out.println("Creator name can't start with a hashtag. Try again: ");
						} else {
							System.out.println("Don't ask for your own videos. Give another channel name: ");
						}
						creator = sc.nextLine();
						flag1 = creator.startsWith("#");
						flag2 = creator.equals(channelName);
					}
					consumer = new Consumer(null, 0, channelName);
					// consumer init: tha pairnei poioi brokers exoyn ayto to channel name (1, 0, 2, 3)
					// kai meta zita sth tyxh apo kapoion ap aytoyw ena video
					// mporei kai mesa sthn connect
					// TODO: we should probably care about the IPs and the ports to work
					// TODO: close streams(here and elsewhere)
					// from a distance *Cons*
					consumer.connectCons(creator);
					consumer = null;
					break; // break the switch
				case 3:
					System.out.println("Hashtag: ");
					String hashtag = sc.nextLine();
					hashtag = Utilities.addHashtag(hashtag);
					consumer = new Consumer(null, 0, channelName);
					// TODO: we should probably care about the IPs and the ports to work
					// from a distance *Cons*
					// hash to hashtag kai zita apo ton antistoixo broker
					consumer.connectCons(hashtag);
					consumer = null;
					break; // break the switch
				default:
					sc.close();
					System.exit(0);
			}
		}
	}
	
	private static int menu() {
		System.out.println("---------- MENU ----------");
		System.out.println("1\tUpload videos"); // send all your videos to the Broker
		System.out.println("2\tSearch creator"); // self explanatory
		System.out.println("3\tSearch hashtag"); // self explanatory
		System.out.println("0\tExit App");
		System.out.println("--------------------------");
		
		System.out.print("Enter: ");
		String userInput = sc.nextLine();
		return userInput != null ? Integer.parseInt(userInput) : 0;        //TODO: if there are no videos, the user can't choose 2 or 3
	}
	
}


// import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import components.*;

public class AppNodeMain {

	static Scanner sc = new Scanner(System.in);
	static Publisher publisher = null;
	static Consumer consumer = null;

	public static void main(String[] args) {

		while (true) {
			int input = menu();
			switch (input) {
			case 1:
				System.out.println("Add your channelName: ");
				String channelName = sc.nextLine();
				publisher = new Publisher(null, channelName, 0, null); // TODO: we should probably care about the IPs
																		// and the ports to work from a distance *Pub*
				publisher.connectPub();
				// i guess after that, the switch gets you back to the menu?

			case 2:
				System.out.println("Creator's name: ");
				String creator = sc.nextLine();
				consumer = new Consumer(null, 0); // TODO: we should probably care about the IPs and the ports to work
													// from a distance *Cons*
				consumer.connectCons(creator);
				// i guess after that, the switch gets you back to the menu?

			case 3:
				System.out.println("Hashtag: ");
				String hashtag = sc.nextLine();
				consumer = new Consumer(null, 0); // TODO: we should probably care about the IPs and the ports to work
													// from a distance *Cons*
				consumer.connectCons(consumer.addHashtag(hashtag)); // a little complicated but it just adds a # to the
																	// string before it sends it
				// i guess after that, the switch gets you back to the menu?

			default:
				System.exit(0);

			}
		}

		// System.out.println(menu());
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
//		sc.close();
		return userInput != null ? Integer.parseInt(userInput) : 0;		//TODO: if there are no videos, the user can't choose 2 or 3
	}

}

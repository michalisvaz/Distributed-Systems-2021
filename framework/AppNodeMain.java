import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import components.*;
import utilities.Utilities;

public class AppNodeMain {
	
	static Scanner sc = new Scanner(System.in);
	static Publisher publisher = null;
	static Consumer consumer = null;
	static ArrayList<Broker> brokers = null;
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Give at least two cmd args. The file with the brokers info and " +
					"the lines which correspond to the current brokers");
			System.exit(-1);
		}
		brokers = initBrokerList(args);
		if (brokers == null) {
			System.exit(-1);
		}
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
					publisher = new Publisher(null, channelName, 0);
					boolean pubInitFlag = publisher.init();
					if (pubInitFlag){
						publisher.readFile();
						if (publisher.getCurrentVideo()==null) {
							System.out.println("Video Upload cancelled");
						}else {
							boolean pubToBrokerSuccess = publisher.sendVideo();
							if (pubToBrokerSuccess) {
								System.out.println("Video successfully uploaded");
							}else{
								System.out.println("Problem in uploading video");
							}
						}
					}else {
						System.out.println("Publisher initialization failed");
					}
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
	
	// initialize brokers list
	private static ArrayList<Broker> initBrokerList(String[] args) {
		String brokerFileName = args[0];
		int[] linesWithBroker = new int[args.length - 1];
		// Open file (with the necessary checks)
		File file = new File(brokerFileName);
		if (!file.exists()) {
			Utilities.printError("File not Found");
			return null;
		} else if (file.isDirectory()) {
			Utilities.printError("Directory instead of file");
			return null;
		}
		Scanner input = null;
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		// initialize and sort the linesWithBroker Array
		for (int i = 1; i < args.length; i++) {
			try{
				linesWithBroker[i-1] = Integer.parseInt(args[i]);
			}catch (NumberFormatException e){
				System.out.println("Give only numbers for lines of the file");
				return null;
			}
		}
		Arrays.sort(linesWithBroker);
		// fill the list with the brokers
		ArrayList<Broker> brokersList = new ArrayList<Broker>();
		int index = 0, cnt = 1;
		while (input.hasNextLine()) {
			String line = input.nextLine();
			if(linesWithBroker[index] == cnt){
				if (Utilities.checkBrokerInfo(line)) {
					String[] parts = line.split(";");
					String ip = parts[0];
					int p1 = Integer.parseInt(parts[1]);
					int p2 = Integer.parseInt(parts[2]);
					Broker tmp = new Broker(ip, p1, p2);
					brokersList.add(tmp);
					index++;
					if (index> linesWithBroker.length){
						break;
					}
				}else {
					System.out.println("Wrong line format at line " + cnt);
					return null;
				}
			}
			cnt++;
		}
		if(brokersList.isEmpty()){
			System.out.println("No brokers found");
			return null;
		}
		return brokersList;
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
		return userInput != null ? Integer.parseInt(userInput) : 0;
	}
	
}

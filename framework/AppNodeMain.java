import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
			System.out.println("Give at least two cmd args. The file with the brokers info and "
					+ "the lines which correspond to the current brokers");
			System.exit(-1);
		}
		brokers = initBrokerList(args);
		Collections.sort(brokers);
		if (brokers == null) {
			System.exit(-1);
		}
		String channelName = null;
		String folderName = null;
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
					channelName = channelName.trim();
					publisher = new Publisher(null, channelName, 0);
					boolean pubInitFlag = publisher.init(brokers);
					if (pubInitFlag) {
						publisher.readFile();
						if (publisher.getCurrentVideo() == null) {
							System.out.println("Video Upload cancelled");
						} else {
							boolean pubToBrokerSuccess = publisher.push();
							if (pubToBrokerSuccess) {
								System.out.println("Video successfully uploaded");
							} else {
								System.out.println("Problem in uploading video");
							}
						}
					} else {
						System.out.println("Publisher initialization failed");
					}
					publisher = null;
					break; // break the switch
				case 2:
					if(folderName==null && channelName!=null) {
						System.out.println("Give folder in which I will save the videos you will get: ");
						folderName = sc.nextLine();
					}else {
						folderName = "Consumer" + channelName;
					}
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
					creator = creator.trim();
					consumer = new Consumer(null, 0, channelName);
					boolean foundResponsibleBroker = consumer.findBroker(brokers, creator);
					if (foundResponsibleBroker) {
						boolean foundVideo = consumer.getByChannel(creator);
						if (foundVideo) {
							consumer.writeVideoFile(folderName);
							System.out.println("Video saved in folder: " + folderName);
						} else {
							System.out.println("Couldn't find videos from requested channel");
						}
					} else {
						System.out.println("Couldn't locate responsible broker");
					}
					consumer = null;
					break; // break the switch
				case 3:
					if(folderName==null && channelName!=null) {
						System.out.println("Give folder in which I will save the videos you will get: ");
						folderName = sc.nextLine();
					}else {
						folderName = "Consumer" + channelName;
					}
					System.out.println("Hashtag: ");
					String hashtag = sc.nextLine();
					hashtag = Utilities.addHashtag(hashtag);
					consumer = new Consumer(null, 0, channelName);
					boolean foundVideo = consumer.getByHashtag(hashtag, brokers);
					if (foundVideo) {
						consumer.writeVideoFile(folderName);
					} else {
						System.out.println("Couldn't find videos with the requested hashtag");
					}
					consumer = null;
					break; // break the switch
				default:
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
			try {
				linesWithBroker[i - 1] = Integer.parseInt(args[i]);
			} catch (NumberFormatException e) {
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
			if (linesWithBroker[index] == cnt) {
				if (Utilities.checkBrokerInfo(line)) {
					String[] parts = line.split(";");
					String ip = parts[0];
					int p1 = Integer.parseInt(parts[1]);
					int p2 = Integer.parseInt(parts[2]);
					Broker tmp = new Broker(ip, p1, p2);
					brokersList.add(tmp);
					index++;
					if (index >= linesWithBroker.length) {
						break;
					}
				} else {
					System.out.println("Wrong line format at line " + cnt);
					return null;
				}
			}
			cnt++;
		}
		if (brokersList.isEmpty()) {
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

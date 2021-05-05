import components.Broker;
import utilities.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class BrokerMain {
	public static void main(String[] args) {
		// args and file format checks. Not the main part of the project
		if (args.length <= 2) {
			System.err.println("Give at least three cmd arguments: " +
					"the file with the brokers, the line of the file that corresponds to your broker" +
					" and the lines of the file which correspond to the other brokers");
			System.exit(-1);
		}
		String brokersFileName = args[0];
		File brokersTextFile = new File(brokersFileName);
		String ip = null;
		int portToPublishers = 0, portToConsumers = 0;
		if (brokersTextFile.exists() && !brokersTextFile.isDirectory()) {
			int lineNumber = Integer.parseInt(args[1]);
			try {
				String line = Files.readAllLines(Paths.get(brokersFileName)).get(lineNumber-1);
				if (Utilities.checkBrokerInfo(line)) {
					String[] parts = line.split(";");
					ip = parts[0];
					portToPublishers = Integer.parseInt(parts[1]);
					portToConsumers = Integer.parseInt(parts[2]);
				} else {
					System.err.println("Invalid info for the broker");
					System.exit(-1);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			System.err.println("Invalid file name");
			System.exit(-1);
		}
		// initialize and run the broker
		Broker broker = new Broker(ip, portToPublishers, portToConsumers);
		broker.setOtherBrokers(initBrokerList(args));
		broker.runBroker();
	}
	
	/**
	 * Initialize brokers list using the file and the lines specified in args
	 *
	 * @param args the command line arguments from main. They must be a filename and the lines of the filename that correspond to the brokers currently in action
	 * @return an ArrayList with the brokers. If anything went wrong return null.
	 */
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
		// fill the list with the brokers while doing the necessary checks for each line
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
}

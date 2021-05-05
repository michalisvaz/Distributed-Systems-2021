import components.Broker;
import utilities.Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BrokerMain {
	public static void main(String[] args) {
		// args and file format checks. Not the main part of the project
		if (args.length != 2) {
			System.err.println("Give two cmd arguments: the file with brokers and the line "
					+ "of the file which corresponds to this broker");
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
		broker.runBroker();
	}
}

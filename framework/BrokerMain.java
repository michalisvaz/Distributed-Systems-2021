import components.Broker;

public class BrokerMain {
	public static void main(String[] args) {
		Broker broker = new Broker(4321);
		broker.openBroker();
		// Ena Servsock gia clietns ki ena gia brokers
		// HashMap fullname -> VideoFile
		// HashMap hashtag -> fullname
		// HashMap channel -> fullname

	}
}

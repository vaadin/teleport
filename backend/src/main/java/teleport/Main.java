package teleport;


public class Main {

	public static void main(String[] args) throws InterruptedException {
		DroneCommandSender sender = new DroneCommandSender();
		sender.executeCommand(new TakeOffCommand());
		Thread.sleep(5000);
		sender.executeCommand(new LandingCommand());
	}
}

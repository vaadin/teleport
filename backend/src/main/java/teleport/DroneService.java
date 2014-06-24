package teleport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DroneService {

	@Autowired
	private DroneCommandSender commandSender;

	public void executeCommand(DroneCommand command) {
		commandSender.executeCommand(command);
	}

	public void takeOff() {
		commandSender.executeCommand(new TakeOffCommand());
	}

	public void land() {
		commandSender.executeCommand(new LandingCommand());
	}
}

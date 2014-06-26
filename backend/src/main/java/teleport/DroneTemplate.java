package teleport;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DroneTemplate {
	private DroneCommandExecuter commandExecuter;

	public DroneTemplate() {
		commandExecuter = new DroneCommandExecuter();
		commandExecuter.start();
	}

	public void changeAltitude(float f) {
		commandExecuter.changeAltitude(f);
	}

	public void rotateByAxis(float f) {
		commandExecuter.rotateByAxis(f);
	}

	public void moveByAxis(float pitch, float roll) {
		commandExecuter.moveByAxis(pitch, roll);
	}

	public void takeOff() {
		commandExecuter.takeOff();
	}

	public void land() {
		commandExecuter.land();
	}
}

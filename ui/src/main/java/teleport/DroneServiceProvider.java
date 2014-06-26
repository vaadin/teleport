package teleport;

public interface DroneServiceProvider {
	public void takeOff();

	public void land();

	public void moveByAxis(float pitch, float roll);

	public void rotateByAxis(float x);

	public void changeAltitudeByAxis(float y);
}

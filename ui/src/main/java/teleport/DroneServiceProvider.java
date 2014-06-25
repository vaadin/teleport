package teleport;

public interface DroneServiceProvider {
	public void takeOff();
	public void land();
	
	public void up();
	public void down();
	public void rotateLeft();
	public void rotateRight();
	
	public void forward();
	public void backward();
	public void left();
	public void right();
}

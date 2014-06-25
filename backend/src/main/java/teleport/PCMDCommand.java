package teleport;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class PCMDCommand implements DroneCommand {

	protected abstract String wrap(int speed, int commandSequenceNumber);

	private ByteBuffer bb = ByteBuffer.allocate(4);

	private FloatBuffer fb;
	private IntBuffer ib;

	public PCMDCommand() {
		fb = bb.asFloatBuffer();
		ib = bb.asIntBuffer();
	}

	protected int intOfFloat(float f) {
		fb.put(0, f);
		return ib.get(0);
	}

	@Override
	public String name() {
		return getClass().getSimpleName();
	}
}

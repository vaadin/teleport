package teleport;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

class PcmdCommand extends DroneCommand {

    private float gaz, yaw, pitch, roll;

    @Override
    protected String buildParameters() {
        float[] vals = {pitch, roll, gaz, yaw};
        String[] intVals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            intVals[i] = vals[i] == 0 ? "0" : Integer.toString(intOfFloat(vals[i]));
        }
        return "1," + String.join(",", intVals);
    }

    public PcmdCommand(int commandSeqNo, float pitch, float roll, float gaz, float yaw) {
        super(CommandType.PCMD, commandSeqNo);
        this.pitch = pitch;
        this.roll = roll;
        this.gaz = gaz;
        this.yaw = yaw;
        
        buildCommand();
    }

    private int intOfFloat(float f) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        IntBuffer ib = bb.asIntBuffer();
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(0, f);
        return ib.get(0);
    }
}

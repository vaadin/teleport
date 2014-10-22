package com.drone.command;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.springframework.core.Ordered;

public class PcmdCommand extends DroneCommand {

    private float gaz, yaw, pitch, roll;

    public PcmdCommand(float pitch, float roll, float gaz, float yaw) {
        super(CommandType.PCMD);
        this.pitch = pitch;
        this.roll = roll;
        this.gaz = gaz;
        this.yaw = yaw;

    }

    @Override
    protected String buildParameters() {
        float[] vals = { pitch, roll, gaz, yaw };
        String[] intVals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            intVals[i] = vals[i] == 0 ? "0" : Integer
                    .toString(intOfFloat(vals[i]));
        }
        return "1," + String.join(",", intVals);
    }

    private int intOfFloat(float f) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        IntBuffer ib = bb.asIntBuffer();
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(0, f);
        return ib.get(0);
    }

    public boolean isStationary() {
        return pitch == 0 && gaz == 0 && yaw == 0 && roll == 0;
    }

    @Override
    public boolean isRepeated() {
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE - 1;
    }
}

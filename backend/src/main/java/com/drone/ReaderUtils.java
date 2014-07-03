package com.drone;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;


public class ReaderUtils {

    public static float[] getFloat(ByteBuffer b, int n) {
        float f[] = new float[n];
        for (int k = 0; k < f.length; k++) {
            f[k] = b.getFloat();
        }
        return f;
    }

    public static int[] getInt(ByteBuffer b, int n) {
        int i[] = new int[n];
        for (int k = 0; k < i.length; k++) {
            i[k] = b.getInt();
        }
        return i;
    }

    public static short[] getShort(ByteBuffer b, int n) {
        short s[] = new short[n];
        for (int k = 0; k < s.length; k++) {
            s[k] = b.getShort();
        }
        return s;
    }

    public static int[] getUInt16(ByteBuffer b, int n) {
        int i[] = new int[n];
        for (int k = 0; k < i.length; k++) {
            i[k] = getUInt16(b);
        }
        return i;
    }

    public static short[] getUInt8(ByteBuffer b, int n) {
        short s[] = new short[n];
        for (int k = 0; k < s.length; k++) {
            s[k] = getUInt8(b);
        }
        return s;
    }

    public static boolean getBoolean(ByteBuffer b) {
        return (b.getInt() == 1);
    }

    public static int getSeconds(int time) {
        int seconds = (time >>> 11);
        return seconds;
    }

    public static  int getUSeconds(int time) {
        int useconds = (time & (0xFFFFFFFF >>> 11));
        return useconds;
    }

    /*
     * Since Java does not have unsigned bytes, all uint8 are converted to signed shorts
     */
    public static short getUInt8(ByteBuffer b) {
        return (short) (b.get() & 0xFF);
    }

    /*
     * Since Java does not have unsigned shorts, all uint16 are converted to signed integers
     */
    public static int getUInt16(ByteBuffer b) {
        return (b.getShort() & 0xFFFF);
    }

    /*
     * Since Java does not have unsigned ints, all uint32 are converted to signed longs
     */
    public static long getUInt32(ByteBuffer b) {
        return (b.getInt() & 0xFFFFFFFFL);
    }

    public static void checkEqual(int expected, int actual, String message) {
        if (expected != actual) {
            throw new RuntimeException(
                    message + " : expected " + expected + ", was " + actual);
        }
    }

    public static int getCRC(byte[] b, int offset, int length) {
        CRC32 cks = new CRC32();
        cks.update(b, offset, length);
        return (int) (cks.getValue() & 0xFFFFFFFFL);
    }

    public static int getCRC(ByteBuffer b, int offset, int length) {
        return getCRC(b.array(), b.arrayOffset() + offset, length);
    }

}

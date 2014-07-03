package com.drone;

import static com.drone.ReaderUtils.getBoolean;
import static com.drone.ReaderUtils.getFloat;
import static com.drone.ReaderUtils.getInt;
import static com.drone.ReaderUtils.getSeconds;
import static com.drone.ReaderUtils.getShort;
import static com.drone.ReaderUtils.getUInt16;
import static com.drone.ReaderUtils.getUInt32;
import static com.drone.ReaderUtils.getUInt8;
import static com.drone.ReaderUtils.getUSeconds;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.drone.command.ConfigCommand;
import com.drone.command.ControlCommand;
import com.drone.command.ControlCommand.ControlType;
import com.drone.command.DroneCommand;
import com.drone.command.LandCommand;
import com.drone.command.ResetEmergencyCommand;
import com.drone.command.ResetWatchdogCommand;
import com.drone.command.TakeOffCommand;

public class DroneTemplate implements InitializingBean, DisposableBean {

    public static final String DEFAULT_IP = "192.168.1.1";
    public static final int DEFAULT_PORT = 5556;
    public static final int DEFAULT_DATA_PORT = 5554;

    private static final int DEFAULT_COMMAND_FPS = 10;

    private final AsyncTaskExecutor taskExecutor;

    // all of these variables may be read from a thread
    // they won't be updated by more than one
    // client, of course, so no need for synchronization
    private volatile boolean commandRunner;
    private volatile int commandSleep;
    private volatile float gaz, pitch, roll, yaw;
    private volatile float velocityMultiplier;
    private int commandSequenceNo = /* 10 */1; // todo
    private int dataPort = DEFAULT_DATA_PORT;
    private List<DroneStateChangeCallback> stateChangeCallbacks = new ArrayList<>();
    private DatagramSocket commandSocket;

    private DatagramSocket dataSocket;

    private InetAddress address;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Runnable commandRunnable = () -> {
        /*-
         try {
         while (this.commandRunner) {

         Optional.ofNullable(getLatestState()).ifPresent(
         s -> stateChangeCallbacks.forEach(scb -> scb
         .onDroneStateChanged(s)));

         try {
         if (isStationary()) {
         executeCommand(new HoverCommand(
         nextCommandSequenceNumber()));
         } else {
         if (pitch != 0 || roll != 0 || yaw != 0) {
         executeCommand(new MoveByAxisCommand(
         nextCommandSequenceNumber(), pitch, roll,
         yaw, velocityMultiplier));
         }
         if (gaz != 0) {
         executeCommand(new ChangeAltitudeCommand(
         nextCommandSequenceNumber(), gaz));
         }
         }

         TimeUnit.MILLISECONDS.sleep(this.commandSleep);
         } catch (InterruptedException ignored) {

         logger.debug("InterruptedException ", ignored);
         }
         }
         } catch (Exception e) {
         logger.error("Error in main loop", e);
         } finally {
         executeCommand(new LandCommand(nextCommandSequenceNumber()));
         }
         -*/
    };

    public DroneTemplate(AsyncTaskExecutor taskExecutor,
            DroneStateChangeCallback... callbacks) throws UnknownHostException {
        this(null, DEFAULT_COMMAND_FPS, taskExecutor, callbacks);
    }

    public DroneTemplate(DroneStateChangeCallback... callbacks)
            throws UnknownHostException {
        this(new SimpleAsyncTaskExecutor(), callbacks);
    }

    public DroneTemplate(String ip, int fps, AsyncTaskExecutor taskExecutor,
            DroneStateChangeCallback... callbacks) throws UnknownHostException {
        this.taskExecutor = taskExecutor;
        Assert.notNull(this.taskExecutor, "you must specify a TaskExecutor!");

        stateChangeCallbacks.addAll(Arrays.asList(callbacks));

        ip = StringUtils.hasText(ip) ? ip : DEFAULT_IP;
        StringTokenizer st = new StringTokenizer(ip, ".");

        byte[] ipBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
        }

        address = InetAddress.getByAddress(ipBytes);

        setCommandFPS(fps);
    }

    public void setCommandFPS(int fps) {
        commandSleep = 1000 / fps;
    }

    private DatagramSocket connect(int port, boolean tickle, int timeoutSeconds)
            throws Exception {
        DatagramSocket ds = new DatagramSocket(port);
        ds.setSoTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));

        if (tickle) {
            ticklePort(ds, port);
        }

        return ds;
    }

    public void stop() {
        commandRunner = false;
    }

    protected int nextCommandSequenceNumber() {
        return commandSequenceNo++;
    }

    protected void executeCommand(DroneCommand command) {
        try {
            String stringRepresentation = command.toString();
            byte[] buffer = stringRepresentation.getBytes();

            logger.info("Command packet " + stringRepresentation);

            commandSocket.send(new DatagramPacket(buffer, buffer.length,
                    address, DEFAULT_PORT));
        } catch (IOException e) {
            logger.error("Failed to execute command", e);
        }
    }

    private void theirTicklePort(DatagramSocket socket, int port) {
        byte[] buf = { 0x01, 0x00, 0x00, 0x00 };
        DatagramPacket packet = new DatagramPacket(buf, buf.length,
                this.address, port);
        try {
            if (socket != null) {
                socket.send(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void ticklePort(DatagramSocket socket, int port)
            throws UnknownHostException {
        byte[] buf = { 0x01, 0x00, 0x00, 0x00 };
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
                port);
        try {
            if (socket != null) {
                socket.send(packet);
            }

        } catch (IOException e) {
            logger.warn("Failed to tickle port " + port, e);
        }
    }

    private int mask;

    private void setMask(boolean reset, int[] tags) {
        int newmask = 0;
        for (int n = 0; n < tags.length; n++) {
            newmask |= 1 << tags[n];
        }
        if (reset) {
            mask &= ~newmask;
        } else {
            mask |= newmask;
        }
        // maskChanged = true;
    }

    private volatile boolean running = true;

    void theirRun() {

        // setMask(true, new int[]{DroneTemplate.DEMO_TAG,
        // DroneTemplate.RAW_MEASURES_TAG});

        int MAX_PACKET_SIZE = 2048;

        try (DatagramSocket datagramSocket = new DatagramSocket(dataPort)) {
            datagramSocket.setSoTimeout(1000);
            this.theirTicklePort(datagramSocket, this.dataPort);

            DatagramPacket packet = new DatagramPacket(
                    new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
            datagramSocket.receive(packet);
            ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0,
                    packet.getLength());

            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int header = buffer.getInt();
            int state = buffer.getInt();
            long sequence = getUInt32(buffer);
            int vision = buffer.getInt();
            DroneState s = new DroneState(state, vision);

            if (s.isNavDataBootstrap()) {
                ConfigCommand configCommand = new ConfigCommand(
                        nextCommandSequenceNumber(), "general:navdata_demo",
                        true);
                String stringRepresentation = configCommand.toString();
                byte[] commandBytes = stringRepresentation.getBytes();

                datagramSocket.send(new DatagramPacket(commandBytes,
                        commandBytes.length, address, DEFAULT_PORT));

                sendControlAck(datagramSocket);
            }

            if (!s.isControlReceived()) {
                sendControlAck(datagramSocket);
            }

            while (running) {
                getLatestState(datagramSocket);

            }
        } catch (Exception e) {
            logger.error("something went wrong", e);
        }

    }

    private void sendControlAck(DatagramSocket datagramSocket)
            throws IOException {
        ControlCommand controlCommand = new ControlCommand(
                nextCommandSequenceNumber(), ControlType.ACK, 0);

        String stringRepresentation = controlCommand.toString();
        byte[] commandBytes = stringRepresentation.getBytes();

        datagramSocket.send(new DatagramPacket(commandBytes,
                commandBytes.length, address, DEFAULT_PORT));
    }

    DroneState parse(ByteBuffer b) {

        b.order(ByteOrder.LITTLE_ENDIAN);
        int magic = b.getInt();
        // checkEqual(0x55667788, magic, "Magic must be correct"); // throws
        // exception, do not know why

        int state = b.getInt();
        long sequence = getUInt32(b);
        System.out.println(sequence);
        int vision = b.getInt();
        DroneState s = new DroneState(state, vision);

        // if (sequence <= lastSequenceNumber && sequence != 1) {
        // // TODO sometimes we seem to receive a previous packet, find out why
        // throw new
        // NavDataException("Invalid sequence number received (received=" +
        // sequence + " last="
        // + lastSequenceNumber);
        // }
        // /lastSequenceNumber = sequence;

        /*
         * for (StateListener aStateListener : stateListener) {
         * aStateListener.stateChanged(s); }
         */

        // parse options
        while (b.position() < b.limit()) {
            int tag = b.getShort() & 0xFFFF;
            int payloadSize = (b.getShort() & 0xFFFF) - 4;
            ByteBuffer optionData = b.slice().order(ByteOrder.LITTLE_ENDIAN);
            payloadSize = Math.min(payloadSize, optionData.remaining());
            optionData.limit(payloadSize);
            parseOption(tag, optionData);
            b.position(b.position() + payloadSize);
        }

        // verify checksum; a bit of a hack: assume checksum = 0 is very
        // unlikely
        /*
         * if (checksum != 0) { checkEqual(getCRC(b, 0, b.limit() - 4),
         * checksum, "Checksum does not match"); checksum = 0; }
         */
        return s;
    }

    int checksum;

    private void parseDemoOption(ByteBuffer b) {
        // if (stateListener.size() > 0 || batteryListener.size() > 0 ||
        // attitudeListener.size() > 0 || altitudeListener.size() > 0
        // || velocityListener.size() > 0 || visionListener.size() > 0) {
        int controlState = b.getInt();

        // batteryPercentage is <=100 so sign is not an issue
        int batteryPercentage = b.getInt();

        float theta = b.getFloat();
        float phi = b.getFloat();
        float psi = b.getFloat();

        int altitude = b.getInt();

        float v[] = getFloat(b, 3);

        System.out.println(String.format(
                "batteryPercentage=%s,theta=%s, phi=%s, psi=%s,altitude=%s",
                batteryPercentage + "", theta + "", phi + "", psi + "",
                altitude + ""));

        @SuppressWarnings("unused")
        long num_frames = getUInt32(b);
        /* Deprecated ! Don't use ! */
        @SuppressWarnings("unused")
        float detection_camera_rot[] = getFloat(b, 9);
        /* Deprecated ! Don't use ! */
        @SuppressWarnings("unused")
        float detection_camera_trans[] = getFloat(b, 3);
        /* Deprecated ! Don't use ! */
        @SuppressWarnings("unused")
        long detection_tag_index = getUInt32(b);

        int detection_camera_type = b.getInt();

        /* Deprecated ! Don't use ! */
        @SuppressWarnings("unused")
        float drone_camera_rot[] = getFloat(b, 9);
        /* Deprecated ! Don't use ! */
        @SuppressWarnings("unused")
        float drone_camera_trans[] = getFloat(b, 3);

        /*
         * if (visionListener.size() > 0 && detection_camera_type != 0) { for
         * (int i=0; i < visionListener.size(); i++)
         * visionListener.get(i).typeDetected(detection_camera_type); }
         * 
         * for (int i=0; i < stateListener.size(); i++)
         * stateListener.get(i).controlStateChanged
         * (ControlState.fromInt(controlState >> 16));
         * 
         * for (int i=0; i < batteryListener.size(); i++)
         * batteryListener.get(i).batteryLevelChanged(batteryPercentage);
         * 
         * for (int i=0; i < attitudeListener.size(); i++)
         * attitudeListener.get(i).attitudeUpdated(theta, phi, psi);
         * 
         * for (int i=0; i < altitudeListener.size(); i++)
         * altitudeListener.get(i).receivedAltitude(altitude);
         * 
         * for (int i=0; i < velocityListener.size(); i++)
         * velocityListener.get(i).velocityChanged(v[0], v[1], v[2]);
         */
    }

    private void parseMagnetoDataOption(ByteBuffer b) {
        // if (magnetoListener.size() > 0) {
        short m[] = getShort(b, 3);

        float[] mraw = getFloat(b, 3);

        float mrectified[] = getFloat(b, 3);

        float m_[] = getFloat(b, 3);

        float heading_unwrapped = b.getFloat();
        float heading_gyro_unwrapped = b.getFloat();
        float heading_fusion_unwrapped = b.getFloat();
        byte calibration_ok = b.get();
        int state = b.getInt(); // TODO: encoding?
        float radius = b.getFloat();
        float error_mean = b.getFloat();
        float error_var = b.getFloat();

        /*
         * MagnetoData md = new MagnetoData(m, mraw, mrectified, m_,
         * heading_unwrapped, heading_gyro_unwrapped, heading_fusion_unwrapped,
         * calibration_ok, state, radius, error_mean, error_var);
         * 
         * for (int i=0; i < magnetoListener.size(); i++)
         * magnetoListener.get(i).received(md);
         */

    }

    private void parseCksOption(ByteBuffer b) {
        checksum = b.getInt();
    }

    private void parseZimmu3000Option(ByteBuffer b) {
        // if (zimmu3000Listener.size() > 0) {

        int vzimmuLSB = b.getInt();
        float vzfind = b.getFloat();

        // for (int i=0; i < zimmu3000Listener.size(); i++)
        // zimmu3000Listener.get(i).received(vzimmuLSB, vzfind);
        // }

    }

    private static final int CKS_TAG = -1;
    private static final int DEMO_TAG = 0;
    private static final int TIME_TAG = 1;
    private static final int RAW_MEASURES_TAG = 2;
    private static final int PHYS_MEASURES_TAG = 3;
    private static final int GYROS_OFFSETS_TAG = 4;
    private static final int EULER_ANGLES_TAG = 5;
    private static final int REFERENCES_TAG = 6;
    private static final int TRIMS_TAG = 7;
    private static final int RC_REFERENCES_TAG = 8;
    private static final int PWM_TAG = 9;
    private static final int ALTITUDE_TAG = 10;
    private static final int VISION_RAW_TAG = 11;
    private static final int VISION_OF_TAG = 12;
    private static final int VISION_TAG = 13;
    private static final int VISION_PERF_TAG = 14;
    private static final int TRACKERS_SEND_TAG = 15;
    private static final int VISION_DETECT_TAG = 16;
    private static final int WATCHDOG_TAG = 17;
    private static final int ADC_DATA_FRAME_TAG = 18;
    private static final int VIDEO_STREAM_TAG = 19;
    private static final int GAMES_TAG = 20;
    private static final int PRESSURE_RAW_TAG = 21;
    private static final int MAGNETO_TAG = 22;
    private static final int WIND_TAG = 23;
    private static final int KALMAN_PRESSURE_TAG = 24;
    private static final int HDVIDEO_STREAM_TAG = 25;
    private static final int WIFI_TAG = 26;
    private static final int ZIMMU_3000_TAG = 27;

    private void parseVisionPerfOption(ByteBuffer b) {
        // if (visionListener.size() > 0) {
        float time_szo = b.getFloat();
        float time_corners = b.getFloat();
        float time_compute = b.getFloat();
        float time_tracking = b.getFloat();
        float time_trans = b.getFloat();
        float time_update = b.getFloat();
        float[] time_custom = getFloat(b, NAVDATA_MAX_CUSTOM_TIME_SAVE);

        /*
         * VisionPerformance d = new VisionPerformance(time_szo, time_corners,
         * time_compute, time_tracking, time_trans, time_update, time_custom);
         * 
         * for (int i=0; i < visionListener.size(); i++)
         * visionListener.get(i).receivedPerformanceData(d); }
         */
    }

    private void parseVisionOption(ByteBuffer b) {
        // if (visionListener.size() > 0) {
        int vision_state = b.getInt();
        int vision_misc = b.getInt();
        float vision_phi_trim = b.getFloat();
        float vision_phi_ref_prop = b.getFloat();
        float vision_theta_trim = b.getFloat();
        float vision_theta_ref_prop = b.getFloat();
        int new_raw_picture = b.getInt();
        float theta_capture = b.getFloat();
        float phi_capture = b.getFloat();
        float psi_capture = b.getFloat();
        int altitude_capture = b.getInt();
        // time in TSECDEC format (see config.h)
        int time_capture = b.getInt();
        int time_capture_seconds = getSeconds(time_capture);
        int time_capture_useconds = getUSeconds(time_capture);
        float[] body_v = getFloat(b, 3);
        float delta_phi = b.getFloat();
        float delta_theta = b.getFloat();
        float delta_psi = b.getFloat();
        int gold_defined = b.getInt();
        int gold_reset = b.getInt();
        float gold_x = b.getFloat();
        float gold_y = b.getFloat();

        // VisionData d = new VisionData(vision_state, vision_misc,
        // vision_phi_trim, vision_phi_ref_prop,
        // vision_theta_trim, vision_theta_ref_prop, new_raw_picture,
        // theta_capture, phi_capture, psi_capture,
        // altitude_capture, time_capture_seconds, time_capture_useconds,
        // body_v, delta_phi, delta_theta,
        // delta_psi, gold_defined, gold_reset, gold_x, gold_y);

        /*
         * for (int i=0; i < visionListener.size(); i++)
         * visionListener.get(i).receivedData(d); }
         */

    }

    private void parseVisionOfOption(ByteBuffer b) {
        // if (visionListener.size() > 0) {
        float[] of_dx = getFloat(b, 5);
        float[] of_dy = getFloat(b, 5);
        /*
         * 
         * for (int i=0; i < visionListener.size(); i++)
         * visionListener.get(i).receivedVisionOf(of_dx, of_dy); }
         */

    }

    private void parseVisionRawOption(ByteBuffer b) {
        // if (visionListener.size() > 0) {
        float[] vision_raw = getFloat(b, 3);
        /*
         * for (int i=0; i < visionListener.size(); i++)
         * visionListener.get(i).receivedRawData(vision_raw); }
         */
    }

    private void parseAltitudeOption(ByteBuffer b) {
        // if (altitudeListener.size() > 0) {
        int altitude_vision = b.getInt();
        float altitude_vz = b.getFloat();
        int altitude_ref = b.getInt();
        int altitude_raw = b.getInt();

        // TODO: what does obs mean?
        float obs_accZ = b.getFloat();
        float obs_alt = b.getFloat();

        float[] obs_x = getFloat(b, 3);

        int obs_state = b.getInt();

        // TODO: what does vb mean?
        float[] est_vb = getFloat(b, 2);

        int est_state = b.getInt();
        /*
         * Altitude d = new Altitude(altitude_vision, altitude_vz, altitude_ref,
         * altitude_raw, obs_accZ, obs_alt, obs_x, obs_state, est_vb,
         * est_state);
         * 
         * for (int i=0; i < altitudeListener.size(); i++)
         * altitudeListener.get(i).receivedExtendedAltitude(d); }
         */
    }

    private void parsePWMOption(ByteBuffer b) {
        // if (pwmlistener.size() > 0) {
        short[] motor = getUInt8(b, 4);
        short[] sat_motor = getUInt8(b, 4);
        float gaz_feed_forward = b.getFloat();
        float gaz_altitude = b.getFloat();
        float altitude_integral = b.getFloat();
        float vz_ref = b.getFloat();
        // pry = pitch roll yaw; bit lazy yes :-)
        int[] u_pry = getInt(b, 3);
        float yaw_u_I = b.getFloat();
        // pry = pitch roll yaw; bit lazy yes :-)
        int[] u_planif_pry = getInt(b, 3);
        float u_gaz_planif = b.getFloat();
        int current_motor[] = getUInt16(b, 4);
        // WARNING: new navdata (FC 26/07/2011)
        float altitude_prop = b.getFloat();
        float altitude_der = b.getFloat();

        /*
         * PWMData d = new PWMData(motor, sat_motor, gaz_feed_forward,
         * gaz_altitude, altitude_integral, vz_ref, u_pry, yaw_u_I,
         * u_planif_pry, u_gaz_planif, current_motor, altitude_prop,
         * altitude_der);
         * 
         * for (int i=0; i < pwmlistener.size(); i++)
         * pwmlistener.get(i).received(d); }
         */
    }

    private void parseRcReferencesOption(ByteBuffer b) {
        // if (referencesListener.size() > 0) {
        int[] rc_ref = getInt(b, 5);
        /*
         * for (int i=0; i < referencesListener.size(); i++)
         * referencesListener.get(i).receivedRcReferences(rc_ref); }
         */
    }

    private void parseTrimsOption(ByteBuffer b) {
        // if (trimsListener.size() > 0) {
        float angular_rates_trim_r = b.getFloat();
        float euler_angles_trim_theta = b.getFloat();
        float euler_angles_trim_phi = b.getFloat();

        /*
         * for (int i=0; i < trimsListener.size(); i++)
         * trimsListener.get(i).receivedTrimData(angular_rates_trim_r,
         * euler_angles_trim_theta, euler_angles_trim_phi); }
         */
    }

    private void parseReferencesOption(ByteBuffer b) {
        // if (referencesListener.size() > 0) {
        int ref_theta = b.getInt();
        int ref_phi = b.getInt();
        int ref_theta_I = b.getInt();
        int ref_phi_I = b.getInt();
        int ref_pitch = b.getInt();
        int ref_roll = b.getInt();
        int ref_yaw = b.getInt();
        int ref_psi = b.getInt();

        float[] v_ref = getFloat(b, 2);
        float theta_mod = b.getFloat();
        float phi_mod = b.getFloat();
        float[] k_v = getFloat(b, 2);
        // assumption: k_mode does not exceed Integer.MAX_INT
        int k_mode = b.getInt();

        float ui_time = b.getFloat();
        float ui_theta = b.getFloat();
        float ui_phi = b.getFloat();
        float ui_psi = b.getFloat();
        float ui_psi_accuracy = b.getFloat();
        int ui_seq = b.getInt();

        /*
         * ReferencesData d = new ReferencesData(ref_theta, ref_phi,
         * ref_theta_I, ref_phi_I, ref_pitch, ref_roll, ref_yaw, ref_psi, v_ref,
         * theta_mod, phi_mod, k_v, k_mode, ui_time, ui_theta, ui_phi, ui_psi,
         * ui_psi_accuracy, ui_seq);
         * 
         * for (int i=0; i < referencesListener.size(); i++)
         * referencesListener.get(i).receivedReferences(d); }
         */

    }

    private void parseEulerAnglesOption(ByteBuffer b) {
        // if (attitudeListener.size() > 0) {
        float theta_a = b.getFloat();
        float phi_a = b.getFloat();

        /*
         * for (int i=0; i < attitudeListener.size(); i++)
         * attitudeListener.get(i).attitudeUpdated(theta_a, phi_a); }
         */

    }

    private void parseTimeOption(ByteBuffer b) {
        // if (timeListener.size() > 0) {
        int time = b.getInt();

        int useconds = getUSeconds(time);
        int seconds = getSeconds(time);

        /*
         * for (int i=0; i < timeListener.size(); i++)
         * timeListener.get(i).timeReceived(seconds, useconds); }
         */
    }

    private void parsePhysMeasuresOption(ByteBuffer b) {
        // if (acceleroListener.size() > 0 || gyroListener.size() > 0) {
        float accs_temp = b.getFloat();
        int gyro_temp = getUInt16(b);

        float[] phys_accs = getFloat(b, NB_ACCS);

        float[] phys_gyros = getFloat(b, NB_GYROS);

        // 3.3volt alim [LSB]
        // TODO: check if LSB indeed means 1 byte
        // assumption alim relates to both sensors
        int alim3V3 = b.getInt() & 0xFF;

        // ref volt Epson gyro [LSB]
        // TODO: check if LSB indeed means 1 byte
        int vrefEpson = b.getInt() & 0xFF;

        // ref volt IDG gyro [LSB]
        // TODO: check if LSB indeed means 1 byte
        int vrefIDG = b.getInt() & 0xFF;

        /*
         * for (int i=0; i < acceleroListener.size(); i++) { AcceleroPhysData d
         * = new AcceleroPhysData(accs_temp, phys_accs, alim3V3);
         * acceleroListener.get(i).receivedPhysData(d); }
         * 
         * for (int i=0; i < gyroListener.size(); i++) { GyroPhysData d = new
         * GyroPhysData(gyro_temp, phys_gyros, alim3V3, vrefEpson, vrefIDG);
         * gyroListener.get(i).receivedPhysData(d); } }
         */

    }

    /* number of trackers in width of current picture */
    private static final int NB_CORNER_TRACKERS_WIDTH = 5;
    /* number of trackers in height of current picture */
    private static final int NB_CORNER_TRACKERS_HEIGHT = 4;

    private static final int DEFAULT_NB_TRACKERS_WIDTH = NB_CORNER_TRACKERS_WIDTH + 1;
    private static final int DEFAULT_NB_TRACKERS_HEIGHT = NB_CORNER_TRACKERS_HEIGHT + 1;

    private static final int NB_NAVDATA_DETECTION_RESULTS = 4;

    // source: navdata_common.h
    private static final int NAVDATA_MAX_CUSTOM_TIME_SAVE = 20;
    private static final int MAX_PACKET_SIZE = 2048;

    private void parseTrackersSendOption(ByteBuffer b) {
        // if (visionListener.size() > 0) {
        //
        // trackers[i][j][0]: locked
        // trackers[i][j][1]: point.x
        // trackers[i][j][2]: point.y

        int[][][] trackers = new int[DEFAULT_NB_TRACKERS_WIDTH][DEFAULT_NB_TRACKERS_HEIGHT][3];
        for (int i = 0; i < DEFAULT_NB_TRACKERS_WIDTH; i++) {
            for (int j = 0; j < DEFAULT_NB_TRACKERS_HEIGHT; j++) {
                trackers[i][j][0] = b.getInt();
            }
        }

        for (int i = 0; i < DEFAULT_NB_TRACKERS_WIDTH; i++) {
            for (int j = 0; j < DEFAULT_NB_TRACKERS_HEIGHT; j++) {
                trackers[i][j][1] = b.getInt();
                trackers[i][j][2] = b.getInt();
            }
        }

        /*
         * // TODO: create Tracker class containing locked + point? for (int
         * i=0; i < visionListener.size(); i++)
         * visionListener.get(i).trackersSend(new TrackerData(trackers)); }
         */
    }

    private void parseVisionDetectOption(ByteBuffer b) {
        // if (visionListener.size() > 0) {
        int ndetected = b.getInt();

        if (ndetected > 0) {

            int type[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);

            // assumption: values are well below Integer.MAX_VALUE, so sign is
            // no issue
            int xc[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);

            // assumption: values are well below Integer.MAX_VALUE, so sign is
            // no issue
            int yc[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);

            // assumption: values are well below Integer.MAX_VALUE, so sign is
            // no issue
            int width[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);

            // assumption: values are well below Integer.MAX_VALUE, so sign is
            // no issue
            int height[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);

            // assumption: values are well below Integer.MAX_VALUE, so sign is
            // no issue
            int dist[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);

            float[] orientation_angle = getFloat(b,
                    NB_NAVDATA_DETECTION_RESULTS);

            // could extend Bytebuffer to read matrix types
            float[][][] rotation = new float[NB_NAVDATA_DETECTION_RESULTS][3][3];
            for (int i = 0; i < NB_NAVDATA_DETECTION_RESULTS; i++) {
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        rotation[i][r][c] = b.getFloat();
                    }
                }
            }

            // could extend Bytebuffer to read vector types
            float[][] translation = new float[NB_NAVDATA_DETECTION_RESULTS][3];
            for (int i = 0; i < NB_NAVDATA_DETECTION_RESULTS; i++) {
                for (int r = 0; r < 3; r++) {
                    translation[i][r] = b.getFloat();
                }
            }

            // assumption: values are well below Integer.MAX_VALUE, so sign is
            // no
            // issue
            int camera_source[] = getInt(b, NB_NAVDATA_DETECTION_RESULTS);
            /*
             * VisionTag[] tags = new VisionTag[ndetected]; for (int i = 0; i <
             * ndetected; i++) { // TODO: does this also contain a mask if not
             * multiple detect? VisionTag tag = new VisionTag(type[i], xc[i],
             * yc[i], width[i], height[i], dist[i], orientation_angle[i],
             * rotation[i], translation[i],
             * DetectionType.fromInt(camera_source[i])); tags[i] = tag; }
             */

            // for (int i=0; i < visionListener.size(); i++)
            // visionListener.get(i).tagsDetected(tags);
        }
    }

    private void parseGyrosOffsetsOption(ByteBuffer b) {
        // if (gyroListener.size() > 0) {
        float[] offset_g = getFloat(b, NB_GYROS);

        // for (int i=0; i < gyroListener.size(); i++)
        // gyroListener.get(i).receivedOffsets(offset_g);
        // }
    }

    private void parseOption(int tag, ByteBuffer optionData) {
        switch (tag) {
        case CKS_TAG:
            parseCksOption(optionData);
            break;
        case DEMO_TAG:
            parseDemoOption(optionData);
            break;
        case TIME_TAG:
            parseTimeOption(optionData);
            break;
        case RAW_MEASURES_TAG:
            parseRawMeasuresOption(optionData);
            break;
        case PHYS_MEASURES_TAG:
            parsePhysMeasuresOption(optionData);
            break;
        case GYROS_OFFSETS_TAG:
            parseGyrosOffsetsOption(optionData);
            break;
        case EULER_ANGLES_TAG:
            parseEulerAnglesOption(optionData);
            break;
        case REFERENCES_TAG:
            parseReferencesOption(optionData);
            break;
        case TRIMS_TAG:
            parseTrimsOption(optionData);
            break;
        case RC_REFERENCES_TAG:
            parseRcReferencesOption(optionData);
            break;
        case PWM_TAG:
            parsePWMOption(optionData);
            break;
        case ALTITUDE_TAG:
            parseAltitudeOption(optionData);
            break;
        case VISION_RAW_TAG:
            parseVisionRawOption(optionData);
            break;
        case VISION_OF_TAG:
            parseVisionOfOption(optionData);
            break;
        case VISION_TAG:
            parseVisionOption(optionData);
            break;
        case VISION_PERF_TAG:
            parseVisionPerfOption(optionData);
            break;
        case TRACKERS_SEND_TAG:
            parseTrackersSendOption(optionData);
            break;
        case VISION_DETECT_TAG:
            parseVisionDetectOption(optionData);
            break;
        case WATCHDOG_TAG:
            parseWatchdogOption(optionData);
            break;
        case ADC_DATA_FRAME_TAG:
            parseAdcDataFrameOption(optionData);
            break;
        case VIDEO_STREAM_TAG:
            parseVideoStreamOption(optionData);
            break;
        case GAMES_TAG:
            parseGamesOption(optionData);
            break;
        case PRESSURE_RAW_TAG:
            parsePressureOption(optionData);
            break;
        case MAGNETO_TAG:
            parseMagnetoDataOption(optionData);
            break;
        case WIND_TAG:
            parseWindOption(optionData);
            break;
        case KALMAN_PRESSURE_TAG:
            parseKalmanPressureOption(optionData);
            break;
        case HDVIDEO_STREAM_TAG:
            parseHDVideoSteamOption(optionData);
            break;
        case WIFI_TAG:
            parseWifiOption(optionData);
            break;
        case ZIMMU_3000_TAG:
            parseZimmu3000Option(optionData);
            break;
        }

    }

    private void parseHDVideoSteamOption(ByteBuffer b) {
        // if (videoListener.size() > 0) {
        // assumption: does not exceed Integer.MAX_INT
        // HDVideoState hdvideo_state = HDVideoState.fromInt(b.getInt());

        // assumption: does not exceed Integer.MAX_INT
        int storage_fifo_nb_packets = b.getInt();

        // assumption: does not exceed Integer.MAX_INT
        int storage_fifo_size = b.getInt();

        // assumption: USB key size below Integer.MAX_INT kbytes
        // USB key in kbytes - 0 if no key present
        int usbkey_size = b.getInt();

        // assumption: USB key size below Integer.MAX_INT kbytes
        // USB key free space in kbytes - 0 if no key present
        int usbkey_freespace = b.getInt();

        // 'frame_number' PaVE field of the frame starting to be encoded for
        // the
        // HD stream
        int frame_number = b.getInt();

        // remaining time in seconds
        int usbkey_remaining_time = b.getInt();

        /*
         * HDVideoStreamData d = new HDVideoStreamData(hdvideo_state,
         * storage_fifo_nb_packets, storage_fifo_size, usbkey_size,
         * usbkey_freespace, frame_number, usbkey_remaining_time);
         * 
         * for (int i=0; i < videoListener.size(); i++)
         * videoListener.get(i).receivedHDVideoStreamData(d); }
         */
    }

    private void parseWifiOption(ByteBuffer b) {
        // TODO: verify if link quality stays below Integer.MAX_INT
        // if (wifiListener.size() > 0) {
        long link_quality = getUInt32(b);
        /*
         * for (int i=0; i < wifiListener.size(); i++)
         * wifiListener.get(i).received(link_quality); }
         */

    }

    private void parseKalmanPressureOption(ByteBuffer b) {
        // if (pressureListener.size() > 0) {

        float offset_pressure = b.getFloat();
        float est_z = b.getFloat();
        float est_zdot = b.getFloat();
        float est_bias_PWM = b.getFloat();
        float est_biais_pression = b.getFloat();
        float offset_US = b.getFloat();
        float prediction_US = b.getFloat();
        float cov_alt = b.getFloat();
        float cov_PWM = b.getFloat();
        float cov_vitesse = b.getFloat();
        boolean effet_sol = getBoolean(b);
        float somme_inno = b.getFloat();
        boolean rejet_US = getBoolean(b);
        float u_multisinus = b.getFloat();
        float gaz_altitude = b.getFloat();
        boolean multisinus = getBoolean(b);
        boolean multisinus_debut = getBoolean(b);

        /*
         * KalmanPressureData d = new KalmanPressureData(offset_pressure, est_z,
         * est_zdot, est_bias_PWM, est_biais_pression, offset_US, prediction_US,
         * cov_alt, cov_PWM, cov_vitesse, effet_sol, somme_inno, rejet_US,
         * u_multisinus, gaz_altitude, multisinus, multisinus_debut);
         * 
         * for (int i=0; i < pressureListener.size(); i++)
         * pressureListener.get(i).receivedKalmanPressure(d); }
         */
    }

    private void parsePressureOption(ByteBuffer b) {
        // if (pressureListener.size() > 0 || temperatureListener.size() > 0) {

        int up = b.getInt();
        short ut = b.getShort();
        int temperature_meas = b.getInt();
        int pression_meas = b.getInt();

        /*
         * for (int i=0; i < pressureListener.size(); i++) { Pressure d = new
         * Pressure(up, pression_meas);
         * pressureListener.get(i).receivedPressure(d); }
         * 
         * for (int i=0; i < temperatureListener.size(); i++) { Temperature d =
         * new Temperature(ut, temperature_meas);
         * temperatureListener.get(i).receivedTemperature(d); } }
         */
    }

    private void parseWindOption(ByteBuffer b) {
        // if (attitudeListener.size() > 0 || windListener.size() > 0) {

        // estimated wind speed [m/s]
        float wind_speed = b.getFloat();

        // estimated wind direction in North-East frame [rad] e.g. if wind_angle
        // is pi/4, wind is from South-West to North-East
        float wind_angle = b.getFloat();

        float wind_compensation_theta = b.getFloat();
        float wind_compensation_phi = b.getFloat();

        float[] state = getFloat(b, 6);

        float[] magneto = getFloat(b, 3);
        /*
         * for (int i=0; i < attitudeListener.size(); i++)
         * attitudeListener.get(i).windCompensation(wind_compensation_theta,
         * wind_compensation_phi);
         * 
         * for (int i=0; i < windListener.size(); i++) { WindEstimationData d =
         * new WindEstimationData(wind_speed, wind_angle, state, magneto);
         * windListener.get(i).receivedEstimation(d); } }
         */

    }

    private void parseGamesOption(ByteBuffer b) {
        // if (counterListener.size() > 0) {

        long double_tap_counter = getUInt32(b);
        long finish_line_counter = getUInt32(b);
        /*
         * Counters d = new Counters(double_tap_counter, finish_line_counter);
         * for (int i=0; i < counterListener.size(); i++)
         * counterListener.get(i).update(d); }
         */

    }

    private void parseVideoStreamOption(ByteBuffer b) {
        // if (videoListener.size() > 0) {

        // quantizer reference used to encode frame [1:31]
        // assumption: sign is irrelevant
        byte quant = b.get();

        // frame size (bytes)
        // assumption: does not exceed Integer.MAX_INT
        int frame_size = b.getInt();

        // frame index
        // assumption: does not exceed Integer.MAX_INT
        int frame_number = b.getInt();

        // atmcd ref sequence number
        // assumption: does not exceed Integer.MAX_INT
        int atcmd_ref_seq = b.getInt();

        // mean time between two consecutive atcmd_ref (ms)
        // assumption: does not exceed Integer.MAX_INT
        int atcmd_mean_ref_gap = b.getInt();

        float atcmd_var_ref_gap = b.getInt();

        // estimator of atcmd link quality
        // assumption: does not exceed Integer.MAX_INT
        int atcmd_ref_quality = b.getInt();

        // drone2

        // measured out throughput from the video tcp socket
        // assumption: does not exceed Integer.MAX_INT
        int out_bitrate = b.getInt();

        // last frame size generated by the video encoder
        // assumption: does not exceed Integer.MAX_INT
        int desired_bitrate = b.getInt();

        // misc temporary data
        int[] temp_data = getInt(b, 5);

        // queue usage
        // assumption: does not exceed Integer.MAX_INT
        int tcp_queue_level = b.getInt();
        // assumption: does not exceed Integer.MAX_INT
        int fifo_queue_level = b.getInt();

        /*
         * VideoStreamData d = new VideoStreamData(quant, frame_size,
         * frame_number, atcmd_ref_seq, atcmd_mean_ref_gap, atcmd_var_ref_gap,
         * atcmd_ref_quality, out_bitrate, desired_bitrate, temp_data,
         * tcp_queue_level, fifo_queue_level);
         * 
         * for (int i=0; i < videoListener.size(); i++)
         * videoListener.get(i).receivedVideoStreamData(d);
         */
        // }
    }

    private void parseWatchdogOption(ByteBuffer b) {
        // if (watchdogListener.size() > 0) {
        int watchdog = b.getInt();
        /*
         * for (int i=0; i < watchdogListener.size(); i++)
         * watchdogListener.get(i).received(watchdog); }
         */
    }

    private void parseAdcDataFrameOption(ByteBuffer b) {
        // if (adcListener.size() > 0) {
        // assumption: does not exceed Integer.MAX_INT or sign is irrelevant
        int version = b.getInt();

        // assumption: sign is irrelevant
        byte[] data_frame = new byte[32];
        b.get(data_frame);

        /*
         * AdcFrame d = new AdcFrame(version, data_frame); for (int i=0; i <
         * adcListener.size(); i++) adcListener.get(i).receivedFrame(d); }
         */
    }

    private static final int NB_ACCS = 3;
    private static final int NB_GYROS = 3;

    private void parseRawMeasuresOption(ByteBuffer b) {
        // if (batteryListener.size() > 0 || acceleroListener.size() > 0 ||
        // gyroListener.size() > 0 || ultrasoundListener.size() > 0) {
        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // speculative: Raw data (10-bit) of the accelerometers multiplied by 4
        int[] raw_accs = getUInt16(b, NB_ACCS);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // speculative: Raw data for the gyros, 12-bit A/D converted voltage of
        // the gyros. X,Y=IDG, Z=Epson
        short[] raw_gyros = getShort(b, NB_GYROS);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // speculative: 4.5x Raw data (IDG), gyro values (x/y) with another
        // resolution (see IDG-500 datasheet)
        short[] raw_gyros_110 = getShort(b, 2);

        // Assumption: value well below Integer.MAX_VALUE
        // battery voltage raw (mV)
        int vbat_raw = b.getInt();

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // probably: Array with starts of echos (8 array values @ 25Hz, 9 values
        // @ 22.22Hz)
        int us_echo_start = getUInt16(b);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // probably: array with ends of echos (8 array values @ 25Hz, 9 values @
        // 22.22Hz)
        int us_echo_end = getUInt16(b);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // Ultrasonic parameter. speculative: echo number starting with 0. max
        // value 3758. examples: 0,1,2,3,4,5,6,7
        // ; 0,1,2,3,4,86,6,9
        int us_association_echo = getUInt16(b);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // Ultrasonic parameter. speculative: No clear pattern
        int us_distance_echo = getUInt16(b);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // Ultrasonic parameter. Counts up from 0 to approx 24346 in 192 sample
        // cycles of which 12 cylces have value
        // 0
        int us_cycle_time = getUInt16(b);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // Ultrasonic parameter. Value between 0 and 4000, no clear pattern. 192
        // sample cycles of which 12 cycles
        // have value 0
        int us_cycle_value = getUInt16(b);

        // see http://blog.perquin.com/blog/ar-drone-navboard/
        // Ultrasonic parameter. Counts down from 4000 to 0 in 192 sample cycles
        // of which 12 cycles have value 0
        int us_cycle_ref = getUInt16(b);
        int flag_echo_ini = getUInt16(b);
        int nb_echo = getUInt16(b);
        long sum_echo = getUInt32(b);
        int alt_temp_raw = b.getInt();
        short gradient = b.getShort();
        /*
         * for (int i=0; i < batteryListener.size(); i++)
         * batteryListener.get(i).voltageChanged(vbat_raw);
         * 
         * for (int i=0; i < acceleroListener.size(); i++) { AcceleroRawData d =
         * new AcceleroRawData(raw_accs);
         * acceleroListener.get(i).receivedRawData(d); }
         * 
         * for (int i=0; i < gyroListener.size(); i++) { GyroRawData d = new
         * GyroRawData(raw_gyros, raw_gyros_110);
         * gyroListener.get(i).receivedRawData(d); }
         * 
         * if (ultrasoundListener.size() > 0) { UltrasoundData d = new
         * UltrasoundData(us_echo_start, us_echo_end, us_association_echo,
         * us_distance_echo, us_cycle_time, us_cycle_value, us_cycle_ref,
         * flag_echo_ini, nb_echo, sum_echo, alt_temp_raw, gradient);
         * 
         * for (int i=0; i < ultrasoundListener.size(); i++)
         * ultrasoundListener.get(i).receivedRawData(d); }
         */

    }

    public DroneState getLatestState(DatagramSocket socket) {
        try {
            DatagramPacket packet = new DatagramPacket(new byte[2048], 2048,
                    address, dataPort);
            socket.receive(packet);

            ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0,
                    packet.getLength());
            DroneState state = parse(buffer);

            if (state.isCommunicationProblemOccurred()) {
                System.err.println("COMMUNICATION ERROR; RESET WATCH DOGGIE");
                ResetWatchdogCommand reset = new ResetWatchdogCommand(
                        nextCommandSequenceNumber());
                String stringRepresentation = reset.toString();
                byte[] commandBytes = stringRepresentation.getBytes();

                socket.send(new DatagramPacket(commandBytes,
                        commandBytes.length, address, DEFAULT_PORT));
            }

            return state;
        } catch (IOException e) {
            logger.warn("Failed to get latest state", e);
            return null;
        }
    }

    private boolean isStationary() {
        return yaw == 0 && gaz == 0 && pitch == 0 && roll == 0;
    }

    public void rotateByAxis(float yaw) {
        this.yaw = yaw;
    }

    public void changeAltitude(float gaz) {
        this.gaz = gaz;
    }

    public void moveByAxis(float pitch, float roll) {
        this.pitch = pitch;
        this.roll = roll;
    }

    public void setVelocity(double velocity) {
        this.velocityMultiplier = (float) (velocity / 100.0);
    }

    public void takeOff() {
        executeCommand(new TakeOffCommand(nextCommandSequenceNumber()));
    }

    public void land() {
        executeCommand(new LandCommand(nextCommandSequenceNumber()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*
         * commandSocket = connect(DEFAULT_PORT, false, 3); dataSocket =
         * connect(DEFAULT_DATA_PORT, true, 3);
         * 
         * commandRunner = true; // todo
         * this.taskExecutor.submit(this.commandRunnable);
         */
        this.taskExecutor.submit(this::theirRun);
    }

    public void resetEmergency() {
        executeCommand(new ResetEmergencyCommand(nextCommandSequenceNumber()));
    }

    @Override
    public void destroy() throws Exception {
        stop();

        if (commandSocket != null) {
            commandSocket.close();
        }
        if (dataSocket != null) {
            dataSocket.close();
        }
    }
}

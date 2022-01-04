package org.jason.fgcontrol.aircraft.c172p.app;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.jason.fgcontrol.aircraft.c172p.C172P;
import org.jason.fgcontrol.connection.sockets.FlightGearInputConnection;
import org.jason.fgcontrol.exceptions.FlightGearSetupException;

/**
 * Rotate the plane heading by 90 degrees a few times. Visually verify in simulator
 * 
 * @author jason
 *
 */
public class ControlInputTest {
    
    private final static String FG_SOCKETS_HOST = "localhost";
    private final static String FG_SOCKET_PROTOCOL_VAR_SEP = ",";
    private final static String FG_SOCKET_PROTOCOL_LINE_SEP = "\n";
    
    private final static String INPUT_KEY = "/orientation/heading-deg";
    
    private final static int FG_SOCKETS_ORIENTATION_INPUT_PORT = 6604;

    private LinkedHashMap<String, String> orientationSchema;

    private FlightGearInputConnection fgSocketsClient;
    
    private C172P plane;
    
    public ControlInputTest() throws FlightGearSetupException, SocketException, UnknownHostException {
        fgSocketsClient = new FlightGearInputConnection(FG_SOCKETS_HOST, FG_SOCKETS_ORIENTATION_INPUT_PORT);
        
        plane = new C172P();
        
        loadControlSchema();
    }
    
    private void loadControlSchema() {

        //key order definitely matters
        orientationSchema = new LinkedHashMap<String, String>();
//        controlSchema.put("/consumables/fuel/tank/level-gal_us", "");
//        controlSchema.put("/consumables/fuel/tank/water-contamination", "");
//        controlSchema.put("/consumables/fuel/tank[1]/level-gal_us", "");
//        controlSchema.put("/consumables/fuel/tank[1]/water-contamination", "");
//        controlSchema.put("/controls/electric/battery-switch", "");
//        controlSchema.put("/controls/flight/aileron", "");
//        controlSchema.put("/controls/flight/auto-coordination", "");
//        controlSchema.put("/controls/flight/auto-coordination-factor", "");
//        controlSchema.put("/controls/flight/elevator", "");
//        controlSchema.put("/controls/flight/flaps", "");
//        controlSchema.put("/controls/flight/rudder", "");
//        controlSchema.put("/controls/flight/speedbrake", "");
//        controlSchema.put("/controls/gear/brake-parking", "");
//        controlSchema.put("/controls/gear/gear-down", "");
//        controlSchema.put("/orientation/alpha-deg", "");
//        controlSchema.put("/orientation/beta-deg", "");
        orientationSchema.put("/orientation/heading-deg", "");
//        controlSchema.put("/orientation/heading-magnetic-deg", "");
        orientationSchema.put("/orientation/pitch-deg", "0");
        orientationSchema.put("/orientation/roll-deg", "0");
//        controlSchema.put("/orientation/track-magnetic-deg", "");
//        controlSchema.put("/orientation/yaw-deg", "");
//        controlSchema.put("/position/altitude-ft", "");
//        controlSchema.put("/position/ground-elev-ft", "");
//        controlSchema.put("/position/latitude-deg", "");
//        controlSchema.put("/position/longitude-deg", "");
//        controlSchema.put("/sim/speed-up", "");
//        controlSchema.put("/sim/freeze/clock", "");
//        controlSchema.put("/sim/freeze/fuel", "");
//        controlSchema.put("/sim/freeze/master", "");
//        controlSchema.put("/velocities/airspeed-kt", "");
//        controlSchema.put("/velocities/groundspeed-kt", "");
//        controlSchema.put("/velocities/vertical-speed-fps", "");
    }
    
    private LinkedHashMap<String, String> copyControlSchema() {
        return new LinkedHashMap<String, String>(orientationSchema);
    }
    
    private void writeInput(LinkedHashMap<String, String> inputHash) throws IOException {
        
        boolean validFieldCount = true;
        
        StringBuilder controlInput = new StringBuilder();
        controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
        
        //foreach key, write the value into a simple unquoted csv string. fail socket write on missing values
        for( Entry<String, String> entry : inputHash.entrySet()) {
            if(orientationSchema.containsKey(entry.getKey())) {
                if(!entry.getValue().equals( "" )) {
                    System.out.println("Value present for key: " + entry.getKey());
                    controlInput.append(entry.getValue());
                }
                else {
                    System.out.println("Unknown value for key: " + entry.getKey());
                    
                    //field count check later
                    validFieldCount = false;
                    break;
                }
                controlInput.append(FG_SOCKET_PROTOCOL_VAR_SEP);
            }
            else {
                System.out.println("Ignoring unknown key: " + entry.getKey());
                validFieldCount = false;
                break;
            }
        }
        controlInput.append(FG_SOCKET_PROTOCOL_LINE_SEP);
        
        if(validFieldCount) {
            int controlInputFields = controlInput.toString().split(FG_SOCKET_PROTOCOL_VAR_SEP).length;
            
            if( controlInputFields - 1 == orientationSchema.size())
            {                
                System.out.println("Writing control input: " + controlInput.toString());
                
                fgSocketsClient.writeControlInput(inputHash);
            }
            else
            {
                System.err.println("Control input had an unexpected number of fields: " + controlInputFields);
            }
        }
        else {
            System.err.println("Control input had an unexpected number of fields: " + validFieldCount);
        }
    }
    
    public void shutdown() {
        //plane shutdown
        if(plane != null) {
            plane.shutdown();
            
        }
    }
    
    public void rotatePlane() throws IOException {
        //"/orientation/heading-deg": 279.981689,
                
        LinkedHashMap<String, String> myInput = copyControlSchema();
        
        myInput.put("/orientation/heading-deg", "90");
        writeInput(myInput);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        myInput.put(INPUT_KEY, "180");
        writeInput(myInput);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        myInput.put(INPUT_KEY, "270");
        writeInput(myInput);
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        myInput.put("/orientation/heading-deg", "0");
        writeInput(myInput);
    }
    
    public static void main(String[] args) throws FlightGearSetupException, SocketException, UnknownHostException {
        
        ControlInputTest app = null;
        
        try {
            app = new ControlInputTest();
            
            //rotate plane 90 degrees a few times
            app.rotatePlane();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (app != null) {
                app.shutdown();
            }
        }
    }
}

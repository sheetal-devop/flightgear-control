package org.jason.flightgear.telnet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightGearManagerTelnet {
    private String host;
    private int port;
    private TelnetClient client;
    private PrintStream clientOutputStream;
    private BufferedInputStream clientInputStream;
    
    private final static int COMMAND_TIMEOUT = 5000;

    private final static int COMMAND_RESPONSE_TIMEOUT = 3000;
    private final static int STARTUP_TIMEOUT = 2000;
    
    private final static String PROPERTY_EQUALS_DELIM = " = ";
    private final static String TELNET_PROMPT = "/>";
    private final static String GET_COMMAND = "get";
    private final static String SET_COMMAND = "set";
    
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearManagerTelnet.class);

    public FlightGearManagerTelnet(String host, int port) throws InvalidTelnetOptionException, IOException {
        this.host = host;
        this.port = port;
        

        this.client = new TelnetClient();
        
        connect();
        
    }
    
    private void connect() throws InvalidTelnetOptionException, IOException {
        final TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        final EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        final SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
        
        client.addOptionHandler(ttopt);
        client.addOptionHandler(echoopt);
        client.addOptionHandler(gaopt);

        client.connect(host, port);
        
        clientOutputStream = new PrintStream( client.getOutputStream() );
        clientInputStream = new BufferedInputStream(client.getInputStream());
        

        LOGGER.info("Starting up telnet connection");
        
        try {
            Thread.sleep(STARTUP_TIMEOUT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        LOGGER.debug("Write initial CR");
        
        
        clientOutputStream.write( "\r\n".getBytes() );
        clientOutputStream.flush();
        
        LOGGER.debug("Wait for initial prompt");
        
        try {
            Thread.sleep(COMMAND_RESPONSE_TIMEOUT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        readUntil("/>", clientInputStream);
        
        LOGGER.info("Telnet session established.");
    }
    
    public void reset() throws InvalidTelnetOptionException, IOException {
        //TODO: disconnect and reconnect
        sendCommand("run reset");
        
        disconnect();
        
        connect();
    }
    
    public void exit() {
        sendCommand("run exit");
        
        disconnect();
    }
    
    public HashMap<String, String> getPropertyValues(String[] propertyNames) {
        return null;
    }
    
    //one-liner script
    public void runNasal(String line) throws IOException {
        runNasal( new String[] {line} );
    }

    //multi-line script
    public void runNasal(String[] lines) throws IOException {
        clientOutputStream.println( "nasal" ) ;
        clientOutputStream.flush();
        
        for(String line : lines) {
            clientOutputStream.println( line ) ;
            clientOutputStream.flush();
        }
        
        clientOutputStream.println( "##EOF##" ) ;
        clientOutputStream.flush();
        
        //possible the input buffer has to be emptied
        readUntil(TELNET_PROMPT, clientInputStream);
    }
    
    //issue get command and read output
    public String getPropertyValue(String propertyName) throws IOException {
        String output = null;
        
        //environment/wind-speed-kt = '3.035655385' (double),
        String rawOutput = sendCommandReadRawOutput(GET_COMMAND + " " + propertyName);
        
        //check that it's something
        if(rawOutput == null || rawOutput.isEmpty()) {
            throw new IOException("Raw command output was null or empty: '" + rawOutput + "'");
        }
        
        //confirm property name is echoed back, and replace
        if(!rawOutput.startsWith(propertyName)) {
            System.err.println("Fail, property name check: " + propertyName + " vs '" + rawOutput + "'");
            throw new IOException("Could not find property value name in output");
        }
        else {
            
            output = rawOutput.substring(propertyName.length());
            
            if(!output.startsWith(PROPERTY_EQUALS_DELIM)) {
                System.err.println("Fail, equals check: " + rawOutput);
                throw new IOException("Could not find equals sign in output");
            }
            
            output = output.substring(PROPERTY_EQUALS_DELIM.length());
            
//            if(!Pattern.matches( " (double)$| (bool)$| (float)$", output ) ) {
//                System.err.println("Fail, type check: " + rawOutput);
//                throw new IOException("Unexpected Type: " + rawOutput);
//            }
            
            //check the type
            if( !( output.endsWith(" (double)") ||
                output.endsWith(" (bool)") || 
                output.endsWith(" (float)") ||
                output.endsWith(" (int)") ||
                output.endsWith(" (string)") || 
                output.endsWith(" (none)") ||
                output.endsWith(" (unspecified)")
                )
            ) {
                
                System.err.println("Fail, type check: " + rawOutput);
                throw new IOException("Unexpected Type: " + rawOutput);
            }
            
            //replace stuff
            //output.replaceAll(" \\(*\\)$", "");
        }
            
        
        
        
        //confirm type at end, in parens, replace
        //replace all whitespace outside of apostrophes
        //replace apostrophes (possibly not if it's a string)
        //>>output should have raw value
        
        int apos_position = output.indexOf("'");
        return output.substring(apos_position + 1, output.indexOf("'", apos_position + 1));
    }
    
    //get just the value
//    public String sendCommandReadOutput(String command) {
//        return "";
//    }
    
    //get the whole string 
    public String sendCommandReadRawOutput(String command) throws IOException {
        //System.out.println("Running command " + command);
        clientOutputStream.println( command ) ;
        clientOutputStream.flush();
        //System.out.println("Command written");
        
        try {
            Thread.sleep(COMMAND_RESPONSE_TIMEOUT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String output = readUntil(TELNET_PROMPT, clientInputStream);

        
        //telnet can output \r and \n, which will both show as newlines in output
        //also replace any leading or trailing whitespace
        return output.replace("\n", "").replace("\r", "").trim();
    }
    
    //send the command. don't care about output, likely will verify in separate command
    public void sendCommand(String command) {
        
    }
    
    public void disconnect() {
        
        if(clientOutputStream != null) {
            clientOutputStream.close();
        }
        
        if(clientInputStream != null) {
            try {
                clientInputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        if(client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private static String readUntil(String pattern, InputStream input) throws IOException {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            int numRead = 0;
            int maxRead = 2000;
  
            char ch = (char) input.read();

            while (true) {
                // System.out.print(ch);
                numRead++;
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {   
                        return sb.toString().replaceAll(TELNET_PROMPT, "");
                    }
                }
                
                if(input.available()==0){
                    break;
                }
                ch = (char) input.read();

                if (numRead > maxRead) {
                    System.out.println("Long read");
                    break; // can not read the pattern
                }
            }
            
            LOGGER.error("Failed to read until TELNET_PROMPT");
            
            //should not get here
            return null;
    }
}

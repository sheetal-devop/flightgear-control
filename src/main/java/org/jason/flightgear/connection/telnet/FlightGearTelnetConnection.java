package org.jason.flightgear.connection.telnet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interact with a FlightGear Telnet service. Mostly used for running nasal scripts.
 * 
 * Property reads and writes are more suited a sockets connection. 
 * 
 * @author jason
 *
 */
public class FlightGearTelnetConnection {
    private String host;
    private int port;
    private TelnetClient client;
    private PrintStream clientOutputStream;
    private BufferedInputStream clientInputStream;

    private final static int COMMAND_RESPONSE_TIMEOUT = 3000;
    private final static int STARTUP_TIMEOUT = 2000;

    private final static String PROPERTY_EQUALS_DELIM = " = ";
    private final static String TELNET_PROMPT = "/>";
    private final static String GET_COMMAND = "get";
    //private final static String SET_COMMAND = "set";
    
    //used from examples
    private final static String TELNET_TERMINAL_TYPE = "VT100";
    
    private final static Logger LOGGER = LoggerFactory.getLogger(FlightGearTelnetConnection.class);

    public FlightGearTelnetConnection(String host, int port) throws InvalidTelnetOptionException, IOException {
        LOGGER.info("Starting up FlightGearManagerTelnet connection");
        
        this.host = host;
        this.port = port;

        this.client = new TelnetClient();
        
        connect();
    }

    private void connect() throws InvalidTelnetOptionException, IOException {
        final TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler(TELNET_TERMINAL_TYPE, false, false, true, false);
        final EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        final SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

        client.addOptionHandler(ttopt);
        client.addOptionHandler(echoopt);
        client.addOptionHandler(gaopt);

        client.connect(host, port);

        clientOutputStream = new PrintStream(client.getOutputStream());
        clientInputStream = new BufferedInputStream(client.getInputStream());

        LOGGER.info("Starting up telnet connection");

        try {
            Thread.sleep(STARTUP_TIMEOUT);
        } catch (InterruptedException e) {
            LOGGER.error("STARTUP_TIMEOUT interrupted", e);
        }

        LOGGER.debug("Write initial CR");

        clientOutputStream.write("\r\n".getBytes());
        clientOutputStream.flush();

        LOGGER.debug("Wait for initial prompt");

        try {
            Thread.sleep(COMMAND_RESPONSE_TIMEOUT);
        } catch (InterruptedException e) {
            LOGGER.error("COMMAND_RESPONSE_TIMEOUT interrupted", e);
        }

        readUntil("/>", clientInputStream);

        LOGGER.info("Telnet session established.");
    }
    
    public void terminateSimulator() throws IOException {
    	runNasal("fgcommand(\"exit\");");
    }
    
    public void resetSimulator() throws IOException {
    	runNasal("fgcommand(\"reset\");");
    }

    public void reset() throws InvalidTelnetOptionException, IOException {

        disconnect();

        connect();
    }

    //exits the telnet session
    public void exit() {
        //sendCommand("run exit");

        disconnect();
    }

    // one-liner script
    public void runNasal(String line) throws IOException {
        runNasal(new String[] { line });
    }

    // multi-line script
    public void runNasal(String[] lines) throws IOException {
        
        LOGGER.debug("Running nasal script");
        
        clientOutputStream.println("nasal");
        clientOutputStream.flush();

        for (String line : lines) {
            clientOutputStream.println(line);
            clientOutputStream.flush();
        }

        clientOutputStream.println("##EOF##");
        clientOutputStream.flush();

        // possible the input buffer has to be emptied
        readUntil(TELNET_PROMPT, clientInputStream);
        
        LOGGER.debug("Nasal script execution completed");
    }

    // issue get command and read output
    public String getPropertyValue(String propertyName) throws IOException {
        String output = null;

        // environment/wind-speed-kt = '3.035655385' (double),
        String rawOutput = sendCommandReadRawOutput(GET_COMMAND + " " + propertyName);

        // check that it's something
        if (rawOutput == null || rawOutput.isEmpty()) {
            throw new IOException("Raw command output was null or empty: '" + rawOutput + "'");
        }

        // confirm property name is echoed back, and replace
        if (!rawOutput.startsWith(propertyName)) {
            LOGGER.error("Fail, property name check: " + propertyName + " vs '" + rawOutput + "'");
            throw new IOException("Could not find property value name in output");
        } else {

            output = rawOutput.substring(propertyName.length());

            if (!output.startsWith(PROPERTY_EQUALS_DELIM)) {
                LOGGER.error("Fail, equals check: " + rawOutput);
                throw new IOException("Could not find equals sign in output");
            }

            output = output.substring(PROPERTY_EQUALS_DELIM.length());

//            if(!Pattern.matches( " (double)$| (bool)$| (float)$", output ) ) {
//                System.err.println("Fail, type check: " + rawOutput);
//                throw new IOException("Unexpected Type: " + rawOutput);
//            }

            // check the type
            if (!(output.endsWith(" (double)") || output.endsWith(" (bool)") || output.endsWith(" (float)")
                    || output.endsWith(" (int)") || output.endsWith(" (string)") || output.endsWith(" (none)")
                    || output.endsWith(" (unspecified)"))) {

                LOGGER.error("Fail, type check: " + rawOutput);
                throw new IOException("Unexpected Type: " + rawOutput);
            }

            // replace stuff
            // output.replaceAll(" \\(*\\)$", "");
        }

        // confirm type at end, in parens, replace
        // replace all whitespace outside of apostrophes
        // replace apostrophes (possibly not if it's a string)
        // >>output should have raw value

        int apos_position = output.indexOf("'");
        return output.substring(apos_position + 1, output.indexOf("'", apos_position + 1));
    }

    // get the whole string
    public String sendCommandReadRawOutput(String command) throws IOException {
        // System.out.println("Running command " + command);
        clientOutputStream.println(command);
        clientOutputStream.flush();
        // System.out.println("Command written");

        try {
            Thread.sleep(COMMAND_RESPONSE_TIMEOUT);
        } catch (InterruptedException e) {
            LOGGER.error("COMMAND_RESPONSE_TIMEOUT interrupted", e);
        }

        String output = readUntil(TELNET_PROMPT, clientInputStream);

        // telnet can output \r and \n, which will both show as newlines in output
        // also replace any leading or trailing whitespace
        return output.replace("\n", "").replace("\r", "").trim();
    }

    // send the command. don't care about output, likely will verify in separate
    // command
    public void sendCommand(String command) {

    }

    public void disconnect() {
        
        LOGGER.debug("Disconnecting telnet connection");
        
        if (clientOutputStream != null) {
            clientOutputStream.close();
        }

        if (clientInputStream != null) {
            try {
                clientInputStream.close();
            } catch (IOException e) {
                LOGGER.warn("IOException on client inputstream close", e);
            }
        }

        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                LOGGER.warn("IOException on TelnetClient close", e);
            }
        }
        
        LOGGER.debug("Telnet connection disconnection completed");
    }

    //TODO: this was found on the internet and it sucks. fix it.
    //Better handling of fire-and-forget script execution like plane startups, 
    //where there really isn't output to care about
    private static String readUntil(String pattern, InputStream input) throws IOException {
        char lastChar = pattern.charAt(pattern.length() - 1);
        StringBuffer sb = new StringBuffer();
        int numRead = 0;
        int maxRead = 2000;

        char ch = (char) input.read();

        while (true) {

            numRead++;
            sb.append(ch);
            if (ch == lastChar) {
                String rawResult = sb.toString();
                if (rawResult.endsWith(pattern)) {
                    return rawResult.replaceAll(pattern, "");
                } else {
                    
                }
            }

            if (input.available() == 0) {
                LOGGER.warn("No input remaining, but haven't found the end pattern");
                break;
            }
            ch = (char) input.read();

            // can not find the pattern
            if (numRead > maxRead) {
                LOGGER.error("Reached read max looking for end pattern");
                break; 
            }
        }

        LOGGER.error("Failed to read until TELNET_PROMPT");

        // should not get here
        return null;
    }
    
    public boolean isConnected() {
        if( this.client != null && this.client.isConnected() ) {
            return true;
        }
        return false;
    }
}

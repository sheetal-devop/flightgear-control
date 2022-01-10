package org.jason.fgcontrol.aircraft.c172p;

//TODO: refactor with changes to FlightGearPlane host/port configuration
public class C172PConfig {

    private String telnetHostname;
    private String socketsHostname;
    private int telnetPort;
    private int socketsPort;
    
    private final static String FG_SOCKETS_DEFAULT_HOST = "localhost";
    private final static int FG_SOCKETS_TELEM_DEFAULT_PORT = 6501;
    
    private final static String FG_TELNET_DEFAULT_HOST = "localhost";
    private final static int FG_TELNET_DEFAULT_PORT = 5501;
    
    public C172PConfig() {
        this(FG_TELNET_DEFAULT_HOST, FG_TELNET_DEFAULT_PORT, FG_SOCKETS_DEFAULT_HOST, FG_SOCKETS_TELEM_DEFAULT_PORT);
    }
    
    public C172PConfig(String telnetHostname, int telnetPort, String socketsHostname, int socketsPort) {
        super();
        this.telnetHostname = telnetHostname;
        this.socketsHostname = socketsHostname;
        this.telnetPort = telnetPort;
        this.socketsPort = socketsPort;
    }
    
    public String getTelnetHostname() {
        return telnetHostname;
    }
    public void setTelnetHostname(String telnetHostname) {
        this.telnetHostname = telnetHostname;
    }
    public String getSocketsHostname() {
        return socketsHostname;
    }
    public void setSocketsHostname(String socketsHostname) {
        this.socketsHostname = socketsHostname;
    }
    public int getTelnetPort() {
        return telnetPort;
    }
    public void setTelnetPort(int telnetPort) {
        this.telnetPort = telnetPort;
    }
    public int getSocketsPort() {
        return socketsPort;
    }
    public void setSocketsPort(int socketsPort) {
        this.socketsPort = socketsPort;
    }
}

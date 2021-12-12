package org.jason.flightgear.connection.telnet.app;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

public class TelnetTelemetryTest {
	public static void main(String[] args) {
		String host = "localhost";
		int port = 5501;
		
		String[] properties = {
			"environment/relative-humidity",
			"environment/effective-visibility-m",
			"environment/temperature-degf",
			"environment/dewpoint-degc",
			"environment/pressure-inhg",
			"environment/visibility-m",
			"environment/gravitational-acceleration-mps2",
			"environment/wind-speed-kt",
			"environment/wind-from-north-fps",
			"environment/wind-from-east-fps",
			"environment/wind-from-down-fps",
			"sim/speed-up"
		};
		
        TelnetClient tc = new TelnetClient();

        final TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        final EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        final SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
		
        try
        {
            tc.addOptionHandler(ttopt);
            tc.addOptionHandler(echoopt);
            tc.addOptionHandler(gaopt);
        }
        catch (final InvalidTelnetOptionException e)
        {
            System.err.println("Error registering option handlers: " + e.getMessage());
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        try {
			tc.connect(host, port);
			
			PrintStream outstr = new PrintStream( tc.getOutputStream() );
			BufferedInputStream instr = new BufferedInputStream(tc.getInputStream());
            
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
            //"\r\n"
            System.out.println("Write initial CR");
           
            
			outstr.write( new String("\r\n").getBytes() );
			outstr.flush();
            
            System.out.println("Wait for initial prompt");
            
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            
            readUntil("/>", instr);
				
			System.out.println("==========");
            for (int i = 0; i < properties.length; i++) {
            	
            	
            	System.out.println("Reading property " + properties[i]);
    			outstr.println( "get " + properties[i]) ;
    			outstr.flush();
    			System.out.println("Command written");
    			
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			String output = readUntil("/>", instr);
    			

    			

    			//String output = "";
//    			while( (ch = instr.read()) != '\n' ) {
//    				//System.out.println("Got " + ch);
//    				output += (char)ch;
//    			}
    			
    			System.out.println("Got value for property " + properties[i] + ": " + output);
            }

            
            tc.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

    private static String readUntil(String pattern, InputStream input) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            int numRead = 0;
            
  
            char ch = (char) input.read();

            while (true) {
                // System.out.print(ch);
                numRead++;
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {                        
                        return sb.toString();
                    }
                }
                
                if(input.available()==0){
                    break;
                }
                ch = (char) input.read();

                if (numRead > 2000) {
                	System.out.println("Long read");
                    break; // can not read the pattern
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

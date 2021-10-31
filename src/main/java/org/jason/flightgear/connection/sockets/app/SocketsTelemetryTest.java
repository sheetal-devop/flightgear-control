package org.jason.flightgear.connection.sockets.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketsTelemetryTest {
	
	private final static String FG_SOCKETS_HOST = "localhost";
	private final static int FG_SOCKETS_PORT = 6501;
	
	public static void main(String[] args) {
		
		Socket fgConnection = null; 
		PrintWriter output = null;
		BufferedReader input = null;
				
		try {
			while(fgConnection == null) {
				try {
					fgConnection = new Socket(FG_SOCKETS_HOST, FG_SOCKETS_PORT);
				}
				catch (ConnectException e) {
					if(e.getMessage().startsWith("Connection refused ")) {
						
						System.err.println("Port is not open, connection refused: " + FG_SOCKETS_PORT);
						e.printStackTrace();
						
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else
					{
						throw e;
					}
				}
			}
				
			
			output = new PrintWriter(fgConnection.getOutputStream(), true);
			input = new BufferedReader( new InputStreamReader(fgConnection.getInputStream()));
			
			String line;
			while( (line = input.readLine()) != null ){
				System.out.println("line: " + line);
			}
			
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			
			if(output != null) {
				output.close();
			}
			
			if(input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(fgConnection != null) {
				try {
					fgConnection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

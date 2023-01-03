package org.jason.fgcontrol.sshd.app;

import java.io.IOException;

import org.jason.fgcontrol.sshd.SSHDServer;

/**
 * Quick client to test the embedded ssh server for edge aircraft.
 * 
 * Seems to hang eclipse so run it from the shell.
 * 
 * @author jason
 *
 */
public class SSHServerApp {
	public static void main(String[] args) {
		
		SSHDServer sshdServer = null;
		try {
			//default
			//sshdServer = new SSHDServer();
			
			//matches existing user
			sshdServer = new SSHDServer("service", "servicepass", 9999, "/tmp/sshd");
		
			sshdServer.start();
			
			System.out.println("SSHD server started");
			
			Thread.sleep(60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(sshdServer != null) {
				sshdServer.shutdown();
			}
		}
	}
}

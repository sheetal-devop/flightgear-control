package org.jason.fgcontrol.sshd.app;

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
			sshdServer = new SSHDServer();
		
			new Thread(sshdServer).start();
		
			Thread.sleep(60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(sshdServer != null) {
				sshdServer.shutdown();
			}
		}
	}
}

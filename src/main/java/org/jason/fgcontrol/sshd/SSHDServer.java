package org.jason.fgcontrol.sshd;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.jason.fgcontrol.aircraft.config.SimulatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launch an SSH server with set credentials and home directory. 
 *
 * TODO: homedir doesn't quite work
 *
 */
public class SSHDServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHDServer.class);

	private final static String DEFAULT_BIND_ADDR = "0.0.0.0";
	
	private String bindAddress;
	private int port;
	private String edgeUser;
	private String edgePass;
	private String homeDir;
	private boolean isRunning;
	private boolean isShutdown;
	
	public SSHDServer() {
		this(
			SimulatorConfig.DEFAULT_SSHD_USER, 
			SimulatorConfig.DEFAULT_SSHD_PASS, 
			SimulatorConfig.DEFAULT_SSHD_PORT, 
			SimulatorConfig.DEFAULT_SSHD_HOME_DIR
		);
	}
	
	public SSHDServer(String user, String pass) {
		this(
			user, 
			pass, 
			SimulatorConfig.DEFAULT_SSHD_PORT, 
			SimulatorConfig.DEFAULT_SSHD_HOME_DIR
		);
	}
	
	public SSHDServer(int port) {
		this(
			SimulatorConfig.DEFAULT_SSHD_USER, 
			SimulatorConfig.DEFAULT_SSHD_PASS,  
			port, 
			SimulatorConfig.DEFAULT_SSHD_HOME_DIR
		);
	}
	
	public SSHDServer(int port, String homeDir) {
		this(
			SimulatorConfig.DEFAULT_SSHD_USER, 
			SimulatorConfig.DEFAULT_SSHD_PASS, 
			port, 
			homeDir
		);
	}
	
	public SSHDServer(String user, String pass, int port, String homeDir) {
		this.edgeUser = user;
		this.edgePass = pass;
		this.port = port;
		this.homeDir = homeDir;
		
		//TODO: config param for this. though probably will always be 0.0.0.0
		setBindAddress(DEFAULT_BIND_ADDR);
	}
	
	public void setBindAddress(String address) {
		this.bindAddress = address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}
		
	private SshServer buildSshServer() {
		SshServer sshdServer = SshServer.setUpDefaultServer();
		
		sshdServer.setHost(bindAddress);
		sshdServer.setPort(port);
				
		sshdServer.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session)
					throws PasswordChangeRequiredException, AsyncAuthException {
				return edgeUser.equals(username) && edgePass.equals(password);
			}
		});
		sshdServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		
		//seems to use user.dir instead of HOME_DIR upon login
		Path homePath = Paths.get(homeDir);
		VirtualFileSystemFactory vfs = new VirtualFileSystemFactory(homePath);
		vfs.setUserHomeDir(edgeUser, homePath);
		
		sshdServer.setFileSystemFactory(vfs);
		
		//will see remote echo if you manually ssh into this
		
		///usr/bin/bash on fedora
		//sshdServer.setShellFactory(new ProcessShellFactory("bash", "/bin/bash", "--no-rc", "-i", "-l" ));
		
		///bin/bash on debian, and option --no-rc is --norc
		//TODO: neofetch and motd
		//"--init-file <(echo \"/usr/bin/neofetch\")",
		sshdServer.setShellFactory(new ProcessShellFactory(
			"bash",
			"/bin/bash",
			"-i"
		));
		
		
		sshdServer.setCommandFactory(new CommandFactory() {

			@Override
			public Command createCommand(ChannelSession channel, String command) throws IOException {
				
				String[] commandFields = command.split(" ");
				
	            return new ProcessShellFactory( commandFields[0], commandFields ).createShell(channel);
			}
			
		});
		
		return sshdServer;
	}

	@Override
	public void run() {
		//start the sshd server
		SshServer sshdServer = buildSshServer();
		
		try {
			sshdServer.start();
			isRunning = true;
			isShutdown = false;
		} catch (IOException e) {
			LOGGER.error("Failed to launch SSHD server", e);
		}
			
		//sleep until shutdown is invoked
		while(isRunning) {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				LOGGER.warn("SSHD server run time sleep interrupted", e);
			}
		}
		
		//exit the sshd server
		
		if (sshdServer != null) {
			try {
				sshdServer.close();
			} catch (IOException e) {
				LOGGER.error("Exception shutting down SSHD server", e);
			}
		}
		
		isShutdown = true;
	}
	
	public void shutdown() {
		
		LOGGER.info("SSHD server shutdown invoked");
		
		isRunning = false;
		
		while(!isShutdown) {
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.warn("SSHD server shut down sleep interrupted", e);
			}
		}
		
		LOGGER.info("SSHD server shutdown completed");
	}
}

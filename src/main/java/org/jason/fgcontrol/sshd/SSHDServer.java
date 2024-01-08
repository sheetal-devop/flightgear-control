package org.jason.fgcontrol.sshd;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.jason.fgcontrol.aircraft.config.SimulatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launch an SSH server with set credentials and home directory. 
 *
 * TODO: homedir doesn't quite work - even test with proper permissions and a real
 * user with a real directory.
 * 
 *
 */
public class SSHDServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHDServer.class);

    // default listen to everything
	private final static String DEFAULT_BIND_ADDR = "0.0.0.0";
	
	private String bindAddress;
	private int port;
	private String edgeUser;
	private String edgePass;
	private String homeDir;
	
	private SshServer sshdServer;
	
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
		
		//skip this method of auth in the challenge
		sshdServer.setPublickeyAuthenticator(null);
		sshdServer.setKeyPairProvider(null);
		
		sshdServer.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session)
					throws PasswordChangeRequiredException, AsyncAuthException {
				return edgeUser.equals(username) && edgePass.equals(password);
			}
		});
		sshdServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		
		//TODO: see if we can get the shell we want this way 
		//sshdServer.setSessionFactory(new SessionFactory(new ServerSessionImpl()));
		
		//seems to use user.dir instead of HOME_DIR upon login
		//no logging around filesystemfactories
		Path homePath = Paths.get(homeDir);
		
		VirtualFileSystemFactory vfs = new VirtualFileSystemFactory(homePath);
		
//		VirtualFileSystemFactory vfs = new VirtualFileSystemFactory() {
//			@Override
//		    public FileSystem createFileSystem(SessionContext session) throws IOException {
//				
//				
//				return null;
//
//			}
//		};
		
		vfs.setUserHomeDir(edgeUser, homePath);
		vfs.setDefaultHomeDir(homePath);
		
		
		
		//vfs.createFileSystem(null);
		
		
		//LOGGER.info("Using homedir for user {}: {}", edgeUser, vfs.getUserHomeDir(edgeUser));

		
		//vfs.setDefaultHomeDir(homePath);
		
		//LOGGER.info("Using homedir for user {}: {}", edgeUser, vfs.getUserHomeDir(edgeUser));
		
		
		
		//will see remote echo if you manually ssh into this
		
		///usr/bin/bash on fedora
		//sshdServer.setShellFactory(new ProcessShellFactory("bash", "/bin/bash", "--no-rc", "-i", "-l" ));
		
		///bin/bash on debian, and option --no-rc is --norc
		//TODO: neofetch and motd
		//"--init-file <(echo \"/usr/bin/neofetch\")",
		
		//working basically, wrong homedir and chars echoed
//		sshdServer.setShellFactory(new ProcessShellFactory(
//			"bash",
//			"/bin/bash",
//			"-i"
//		));
		
		//works-ish
		//whoami is not the logged-in user
//		sshdServer.setShellFactory(new ProcessShellFactory(
//				"bash",
//				"/bin/bash",
//				"--noprofile",
//				"--norc",
//				"-i"
//		));
		
		//no character echo, can't delete typed chars- char code gets entered. still users user.dir as home
		//whoami is not the logged-in user
		/*
		 * $ ls 
			ls: cannot access ''$'\177\177\177\177': No such file or directory

		 * can't run single commands on login (ssh user@host pwd)
		 */
		sshdServer.setShellFactory(new InteractiveProcessShellFactory());
		
		sshdServer.setFileSystemFactory(vfs);
		
		//seems to have no effect- can authenticate and run commands without this
//		sshdServer.setCommandFactory(new CommandFactory() {
//
//			@Override
//			public Command createCommand(ChannelSession channel, String command) throws IOException {
//				
//				String[] commandFields = command.split(" ");
//				
//	            return new ProcessShellFactory( commandFields[0], commandFields ).createShell(channel);
//			}
//			
//		});
		
		return sshdServer;
	}
	
	public void start() throws IOException {
		sshdServer = buildSshServer();
		
		sshdServer.start();
	}
	
	public void shutdown() {
		
		LOGGER.info("SSHD server shutdown invoked");
		
		try {
			sshdServer.close();
		} catch (IOException e) {
			LOGGER.warn("Exception shutting down SSHD Server", e);
		}
		
		LOGGER.info("SSHD server shutdown completed");
	}
}

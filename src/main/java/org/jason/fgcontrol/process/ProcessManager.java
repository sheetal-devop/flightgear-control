package org.jason.fgcontrol.process;

import java.io.File;
import java.io.IOException;

/**
 * Take a shell script that launches fgfs and manage its execution
 * 
 * 
 * @author jason
 *
 */
public class ProcessManager {
    private String scriptFile;
    private long pid;
    
    private final static int STARTUP_TIMEOUT = 60 * 1000;
    private final static int STARTUP_AFTER_PORT_OPEN_WAIT = 5 * 1000;
    
    public ProcessManager(String scriptFile) {
        
        this.scriptFile = new File(scriptFile).getAbsolutePath();
    }
    
    public boolean startApplication() {
        boolean retval = false;
        
        //launch process, wait until ports are open, then wait some more
        
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(scriptFile);
        
        try {
            Process process = pb.start();
            
            //capture output from process and redirect it to a file
            //asynchronously
            //String errorOut = IOUtils.toString(process.getErrorStream());
            
            pid = process.pid();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        
        return isRunning();
    }
    
    public boolean isRunning() {
        
        //check that the pid is still running, and the process matches our script file
        //fgfs can start, get a pid, crash, and have the OS reuse the pid before the next check
        
        return false;
    }
    
    public boolean shutdownApplication() {
        
        //check if it's running
        
        return false;
    }
    
}

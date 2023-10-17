package tape.endit_tss;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


public class Util {
	
	private static final Logger utilLogRead = Logger.getLogger(Util.class.getName()+".UtilR");	
	private static final Logger utilLogWrite = Logger.getLogger(Util.class.getName()+".UtilW");	
	public int pause = 30000; //milliseconds
	
public int getNrOpenProcesses() {
	
	String command = "pgrep tss";
	int count = 0;

try {	    		    		        
    	Process pr = Runtime.getRuntime().exec(command);	       	               	               
        pr.getErrorStream().close();
        pr.getOutputStream().close();
        
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
       
        while ((stdInput.readLine()) != null) {
        	count++; 
        }
        	            	        
        return count-1;
        
    } catch (Exception ex) {
    	if (App.action.equalsIgnoreCase("Read")) 
    		utilLogRead.error("Exception: " + ex + "  cmd: " + command);
    	else 
    		utilLogWrite.error("Exception: " + ex + "  cmd: " + command);
    	
        return count-1;
    }
}


public int limitOpenProcesses(int nrOpenProc, int limit){
	
	nrOpenProc++;		
	
	while (nrOpenProc >= limit) {
		
		try {	
			
			if (App.action.equalsIgnoreCase("Read")) 
				utilLogRead.debug("Limit of active requests:" + limit + " is reached. Pause tss process creation for " +  pause/1000 + " seconds.");			
			else 
				utilLogWrite.debug("Limit of active requests:" + limit + " is reached. Pause tss process creation for " +  pause/1000 + " seconds.");			
			
			Thread.sleep(pause);			
			nrOpenProc = getNrOpenProcesses();
			
					
		} catch (InterruptedException e) {
						
			if (App.action.equalsIgnoreCase("Read")) 
				utilLogRead.error("Interrupted Exception: " + e);			
			else 
				utilLogWrite.error("Interrupted Exception: " + e);
			
		}
		
	}
	
	return nrOpenProc;
	
}

}

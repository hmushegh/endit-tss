package tape.endit_tss;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import tape.endit_tss.Read.ReadAFile;
import tape.endit_tss.Read.MonitoringR;
import tape.endit_tss.Write.MonitoringW;
import tape.endit_tss.Write.WriteAFile;
import tape.endit_tss.Util;


/**
 *  The main class that initiates the entire procedure of reading/writing files from/to tape.
 *   
 * @author Haykuhi Musheghyan, <haykuhi.musheghyan@kit.edu>, KIT
 * @year 2019
 */

public class App {
	
	private static final Logger loggerRead = Logger.getLogger(App.class.getName()+".AppRead");
	private static final Logger loggerWrite = Logger.getLogger(App.class.getName()+".AppWrite");
	    
    private static final long pollingInterval = 5 * 1000; //The monitor will perform polling on the folder every 5 seconds
    
    public static  Path readRequestDir;
    public static  Path readInDir;   
    public static  Path writeOutDir;
    public static  Path writeRequestDir;    
    public static String writeBashScript;
    public static int activeRequest;
    public static String action;
       
     //
    public static void FileWriting() throws Exception  {
    	
    	Config config = new Config();
    	writeRequestDir = config.getWriteRequestDir();
    	writeOutDir = config.getWriteOutDir();
    	writeBashScript = config.getWriteBashScript();
    	
    	activeRequest = config.getActiveRequests();
    	//action = config.getAction();    	
    	action = "Write";
    	
 	   	
    	Util utilObj = new Util();
    	int nrOpenProcesses = utilObj.getNrOpenProcesses();
    	 	
		// Specifies the directory that needed to be watched/monitored.
        final File directory = new File(writeRequestDir.toString());         
                   	       
        // Lists the directory if it is not empty        
		 File[] fileList = directory.listFiles();		  
	       if (fileList != null) {
	    	   // Switches on monitoring of new files
	    	   switchOnFileMonitoring(directory, action);
	    	   for (File file: fileList) {
	               if (file.isFile() && file.length()>0) {
	            	   WriteAFile wFile = new WriteAFile();
	            	   try {
	            		   JsonObject jObj = wFile.getJsonFile(file);     	   	            	   
	            	   // If the tss migrate command exit code is '0', the file is removed from the 'out' directory	            		  
	            		   if (jObj.has("tss-pid-exitValue") && jObj.get("tss-pid-exitValue").getAsInt() == 0){ 
	            	    	 		// Calls the removeFile method
	            					if (wFile.removeFile(file.getName())) {
	            						loggerWrite.info("FileName: " + "\"" + file.getName() + "\"" + " removed from 'out' directory ");
	            					}
	            		   }	            	     
	            		   if (!jObj.has("tss-pid")){
	            			try {	    
	            				 nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, activeRequest);
	            				 wFile.start(file); 	  
	     	           	  }catch (IllegalArgumentException e) {
	     	           		loggerWrite.error("IllegalArgumentException: FileName: " + "\"" + file.getName() + "\"", e);
	     	           	  }
	            		} else {
	            			long pid = jObj.get("tss-pid").getAsLong();
	            			if (wFile.isStillAllive(pid)) {
	            				loggerWrite.info("Tss process is still alive,  skipping the creation of a new process... FileName: " + file.getName() +  " tss-pid: " + pid);            				
	            			}
	            			else {
	            				loggerWrite.info("Tss process 'tss-pid':" + pid + " does not exist anymore, creating a new process... FileName: " + file.getName());
	            				nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, activeRequest);
	            				wFile.start(file); 	            				
	            			}
	            			
	     	           	}
	           		} catch (NullPointerException nEx) {
	           			loggerWrite.error("JSON object is null: FileName: " + "\"" + file.getAbsolutePath() + "\"", nEx);
	            	}
	              }
	           	}
	           }else {
	        	   // Switches on monitoring of new files
	        	   switchOnFileMonitoring(directory, action);
	           }
	    	       
    }
    
    
    //      
    public static void FileReading() throws Exception  {	    	
		    	
    	Config config = new Config();    	
    	readRequestDir = config.getReadRequestDir();    	
     	readInDir = config.getReadInDir();
     	activeRequest = config.getActiveRequests();
     	//action = config.getAction();
     	
     	action = "Read";
     	   	
     	 Util utilObj = new Util();
     	 int nrOpenProcesses = utilObj.getNrOpenProcesses();
     	 
  		// Specifies the directory that needed to be watched/monitored.    	    	 
    	 final File directory = new File(readRequestDir.toString()); 
    	  	   	
          // Lists the directory if it is not empty
  		 File[] fileList = directory.listFiles();		  
  	       if (fileList != null) {
  	    	   
  	    	// Switches on monitoring of new files
  	    	   switchOnFileMonitoring(directory, action);
  	    	  
  	           for (File file: fileList) {  	        	 
  	               if (file.isFile() && file.length()>0) {	  	            	  
  	            	  ReadAFile rObj = new ReadAFile();
  	            	  try {
  	            		  JsonObject jObj = rObj.getJsonFile(file); 	            		
  	            		  if (!jObj.has("tss-pid")){
	            			try {
	            				nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, activeRequest);
	            				rObj.start(file);         
	            				
	     	           	  	}catch (IllegalArgumentException e) {
	     	           	  	loggerRead.error("IllegalArgumentException: FileName: " + "\"" + file.getName() + "\"", e);
	     	           	  	}
  	            		  } else {
  	            			loggerRead.info("'tss-pid' record already exists in the metadata file, skipping the creation of a new tss process..." + "\"" + file.getName() + "\"");	            	    	
	            		} 	            	 	
	            	}catch (NullPointerException nEx) {
	            		loggerRead.error("JSON object is null: FileName:" + "\"" + file.getName() + "\"", nEx);
	            	}  	       
  	               }
  	               
  	              }      
  	            } else {
  	            	 	// Switches on monitoring of new files
  	            	switchOnFileMonitoring(directory, action);
  	       }
    }
    
    
    //
    public static void switchOnFileMonitoring (File directory, String action) throws Exception {
    	
    	 // Create a new FileAlterationObserver on the given directory
        FileAlterationObserver fileObserver = new FileAlterationObserver(directory);
        
        if (directory.getAbsolutePath().contains("wT")){         	
          	fileObserver.addListener(new MonitoringW());
        }else {         	
        	fileObserver.addListener(new MonitoringR());      	
        }
 
        // Create a new FileAlterationMonitor with the given pollingInterval period
        final FileAlterationMonitor monitor = new FileAlterationMonitor(
                pollingInterval);
 
        // Add the previously created FileAlterationObserver to FileAlterationMonitor
        monitor.addObserver(fileObserver);
 
        // Start the FileAlterationMonitor
        monitor.start();
        
        if (action.equalsIgnoreCase("Read"))        
        	loggerRead.info("Start monitoring of '" + directory + "' folder" );
        else 
        	loggerWrite.info("Start monitoring of '" + directory + "' folder" );
        	
    }
    
    
    // Main method
	public static void main(String[] args) throws Exception  {	
	    		
		/*Config config = new Config();    
		String action = config.getAction();
		
		if (action.equalsIgnoreCase("read")) {			
			System.out.println("File reading starts...");
			FileReading();
			
		}else if (action.equalsIgnoreCase("write")) {
			System.out.println("File writing starts...");
			FileWriting();
			
		}else {			
			System.out.println("No argument or wrong argument passed..!");		
		}
		*/
		String str = args[0];
		
		if (str.equalsIgnoreCase("read")) {			
			System.out.println("File reading starts...");
			FileReading();
			
		}else if (str.equalsIgnoreCase("write")) {
			System.out.println("File writing starts...");
			FileWriting();
			
		}else {			
			System.out.println("No argument or wrong argument passed..!");		
		}
				
	}

}

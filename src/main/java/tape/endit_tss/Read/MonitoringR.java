package tape.endit_tss.Read;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import tape.endit_tss.App;
import tape.endit_tss.Util;

// The class is used to handle different actions on the given directory. 
public class MonitoringR implements FileAlterationListener {

	private static final Logger watchReaderLog = Logger.getLogger(MonitoringR.class);
	Util utilObj = new Util();
	int nrOpenProcesses = utilObj.getNrOpenProcesses();
	

	public void onStart(final FileAlterationObserver observer) {
		// watchReaderLog.info("The FileListener has started on "
		// + observer.getDirectory().getAbsolutePath());
	}
 
     
    public void onDirectoryCreate(final File directory) {
    	watchReaderLog.info(directory.getAbsolutePath() + " was created.");
    }
 
     
    public void onDirectoryChange(final File directory) {     
    	watchReaderLog.info(directory.getAbsolutePath() + " was modified."); 
    }
 
     
    public void onDirectoryDelete(final File directory) {    
    	watchReaderLog.info(directory.getAbsolutePath() + " was deleted."); 
    }
 
     
    public void onFileCreate(final File file) {  
    	    	watchReaderLog.info(file.getAbsoluteFile() + " was CREATED. Typically, the new metadata file is written to the '/request' folder."
    			+ " Manual file creation also caught by ENDIT-TSS monitoring ");    	   	
    	if (file.getAbsolutePath().contains("request")) {
    		 ReadAFile rObj = new ReadAFile();
    		 JsonObject jObj = rObj.getJsonFile(file); 
       try {
    		 if (!jObj.has("tss-pid")){
    		 	    		 		 
    			 nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, App.activeRequest);
    			 rObj.start(file);
        	 
    		 } else {
        		 watchReaderLog.info("'tss-pid' record already exists in the metadata file, skipping the creation of a new tss process..." + "\"" + file.getName() + "\"");	            	    	
     		} 
            
             }catch (IllegalArgumentException e) {
    			 watchReaderLog.error("IllegalArgumentException: FileName: " + "\"" + file.getName() + "\"",e);			
    		 }catch (InterruptedException p){
    			 watchReaderLog.error("InterruptedException: FileName: " + "\"" + file.getName() + "\"",p);
    		 }catch (IOException k) {
    			 watchReaderLog.error("IOException: FileName: " + "\"" + file.getName() + "\"",k);
    		 }catch (ExecutionException l) {
    			 watchReaderLog.error("ExecutionException: FileName: "  + "\"" + file.getName() + "\"",l);
    		 }catch (NullPointerException nEx) {
    			 watchReaderLog.error("JSON object is null: FileName: " + "\"" + file.getName() + "\"",nEx);
    		 }
    	    	 	
    	}
    	
    }
      
    public void onFileChange(final File file) {
    	watchReaderLog.info(file.getAbsoluteFile() + " was MODIFIED.  Normally, 'tss-pid' "
    			+ "is set to the metadata file by ENDIT-TSS. Manual changes to the file content also caught by ENDIT-TSS monitoring");    	
    	 
    }
      
    public void onFileDelete(final File file) {
    	watchReaderLog.info(file.getAbsolutePath() + " was DELETED. Normally, it is deleted by dcache ENDIT-Provider. "
    			+ "Manual deletion also caught by ENDIT-TSS monitoring"); 
        
    }
 
     
    public void onStop(final FileAlterationObserver observer) {
    	//watchReaderLog.info("The FileListener has stopped on "
    	//		+ observer.getDirectory().getAbsolutePath());  	
    	
    }   
    
       
     
}

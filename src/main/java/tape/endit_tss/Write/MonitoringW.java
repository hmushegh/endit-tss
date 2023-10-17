package tape.endit_tss.Write;

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
public class MonitoringW implements FileAlterationListener {
	private static final Logger watchWriterLog = Logger.getLogger(MonitoringW.class);
	Util utilObj = new Util();
	int nrOpenProcesses = utilObj.getNrOpenProcesses();
	 
    public void onStart(final FileAlterationObserver observer) {
		//watchWriterLog.info("The FileListener has started on "
		//      + observer.getDirectory().getAbsolutePath() + "\n");
    }
 
     
    public void onDirectoryCreate(final File directory) {
    	watchWriterLog.info(directory.getAbsolutePath() + " was created.");
    }
 
     
    public void onDirectoryChange(final File directory) {   
    	watchWriterLog.info(directory.getAbsolutePath() + " was modified.");
    }
 
     
    public void onDirectoryDelete(final File directory) {        
    	watchWriterLog.info(directory.getAbsolutePath() + " was deleted.");
    }
 
     
    public void onFileCreate(final File file) {  
    	watchWriterLog.info(file.getAbsoluteFile() + " was CREATED. Typically, the new metadata file is written to the '/request' folder."
    			+ " Manual file creation also caught by ENDIT-TSS monitoring "); 
	if (file.getAbsolutePath().contains("request")) {
		
		 WriteAFile wFile = new WriteAFile();
   		 JsonObject jObj = wFile.getJsonFile(file); 
   
   	try {
   		
   	    if (!jObj.has("tss-pid")){
   				 		 
   		 	nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, App.activeRequest);
   			//App.exService.submit(App.createRunnable(file));
				wFile.start(file);
			
   		} else {
			long pid = jObj.get("tss-pid").getAsLong();
			if (wFile.isStillAllive(pid)) {
				watchWriterLog.info("Tss process is still alive,  skipping the creation of a new process... FileName: " + file.getName() +  " tss-pid: " + pid);            				
			}
			else {
				watchWriterLog.info("Tss process 'tss-pid':" + pid + " does not exist anymore, creating a new process... FileName: " + file.getName());
				nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, App.activeRequest);
				wFile.start(file); 
	    		//exService.submit(createRunnable(file));
			}	
   	    
   	    }
   	    
   	} catch (IllegalArgumentException e) {
				watchWriterLog.error("IllegalArgumentException: FileName: " + "\"" + file.getName() + "\"",e);			
   		 }catch (InterruptedException p){
   			watchWriterLog.error("InterruptedException: FileName: " + "\"" + file.getName() + "\"",p);
   		 }catch (IOException k) {
   			watchWriterLog.error("IOException: FileName: " + "\"" + file.getName() + "\"",k);
   		 }catch (ExecutionException l) {
   			watchWriterLog.error("ExecutionException: FileName: "  + "\"" + file.getName() + "\"",l);
   		 }catch (NullPointerException nEx) {
   			watchWriterLog.error("JSON object is null: FileName: " + "\"" + file.getName() + "\"",nEx);
   		 }
   	        //WriteAFile wFile = new WriteAFile();    	 
    
   	 /*else {
			long pid = jObj.get("tss-pid").getAsLong();
			if (wFile.isStillAllive(pid)) {
				watchWriterLog.info("Tss process is still alive,  skipping the creation of a new process... FileName: " + file.getName() +  " tss-pid: " + pid);            				
			}
			else {
				try {
				watchWriterLog.info("Tss process 'tss-pid':" + pid + " does not exist anymore, creating a new process... FileName: " + file.getName());
				nrOpenProcesses = utilObj.limitOpenProcesses(nrOpenProcesses, App.activeRequest);
				wFile.start(file); 
				//exService.submit(createRunnable(file));
			
			} catch (IllegalArgumentException e) {
				watchWriterLog.error("IllegalArgumentException: FileName: " + "\"" + file.getName() + "\"",e);			
			}catch (InterruptedException p){
				watchWriterLog.error("InterruptedException: FileName: " + "\"" + file.getName() + "\"",p);
			}catch (IOException k) {
				watchWriterLog.error("IOException: FileName: " + "\"" + file.getName() + "\"",k);
			}catch (ExecutionException l) {
				watchWriterLog.error("ExecutionException: FileName: "  + "\"" + file.getName() + "\"",l);
			}catch (NullPointerException nEx) {
				watchWriterLog.error("JSON object is null: FileName: " + "\"" + file.getName() + "\"",nEx);
			}
			
     	}
	}*/
   	   	 
	}
	
    }
 
     
    public void onFileChange(final File file) {
    	 watchWriterLog.info(file.getAbsoluteFile() + " was MODIFIED.  Normally, 'tss-pid' "
     			+ "is set to the metadata file by ENDIT-TSS. Manual changes to the file content also caught by ENDIT-TSS monitoring");    
        
    }
 
     
    public void onFileDelete(final File file) {
    	watchWriterLog.info(file.getAbsolutePath() + " was DELETED. Normally, it is deleted by dcache ENDIT-Provider. "
    			+ "Manual deletion also caught by ENDIT-TSS monitoring"); 
        
    }
 
     
    public void onStop(final FileAlterationObserver observer) {
    	//watchWriterLog.info("The FileListener has stopped on "
    	//		+ observer.getDirectory().getAbsolutePath() + "\n");  	
    	
    }   
     
}


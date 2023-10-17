package tape.endit_tss.Write;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import tape.endit_tss.App;

/**
 * The class has a "start(File file)" method, that receives a metadata file.
 *  
 * The program extracts the JSON object from the metadata file, 
 * constructs the corresponding tss command and creates the tss process. 
 * 
 * If the tss process was successfully created, it writes the PID 
 * of the created tss process to the metadata file.
 * 
 * The program waits for the completion of the created tss process
 * and writes the return value of the completed process to the metadata file.
 * 
 * If the return value of the created tss process is 0, then the file
 * will be removed from the "out" directory of the ENDIT-Provider.
 *  
 * If the file no longer exists in the "out" directory of the ENDIT-Provider,
 * then the ENDIT-Provider automatically removes the corresponding metadata file
 * from its "request" directory.
 *  
 * @author Haykuhi Musheghyan, <haykuhi.musheghyan@kit.edu>, KIT
 * @year 2019
 */

public class WriteAFile {
	
		private static final Logger wLogger = Logger.getLogger(WriteAFile.class);
	
		public WriteAFile () {
		
		}
		// Calls all necessary methods (extracts a JSON object from a metadata file, constructs the corresponding tss cmd and creates a tss process). 
				public void start(File file) throws InterruptedException, IOException, IllegalArgumentException, ExecutionException {
				       
			    	JsonObject jObj = getJsonFile(file);	
			    	if (!jObj.isJsonNull()) {
			    		String cmd = getTssCmd(jObj, file);	
			    		//String cmd = getTssCmdTest(jObj, file); // for the test only (w/o dcache)
			    		if (cmd != null) { 					
			    			callAScript(file, cmd);		    			
			    			
			    			
			    		}else {
			    			wLogger.debug("Wrong tss command: " + cmd + " FileName: " + "\"" + file.getName() + "\"");
			    		}
					
			    	}
			    	else {
			    		wLogger.debug("JSON object is: " + jObj + " FileName: " + "\"" + file.getName() + "\"");
			    	}
								
				}    
				
		// Extracts a JSON object from a metadata file and returns it. 
	    public JsonObject getJsonFile(File file) {
	    	
	      	 JsonObject jsonObject = null;
	  	    	  try {  
		    	 	  String fileContent = FileUtils.readFileToString(file, Charsets.UTF_8).trim();
		    	      jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();
		    		  if (!jsonObject.isJsonObject()){
		    			  wLogger.error("Error: Could not convert to JSON object:  FileName: " + "\"" + file.getName() + "\"");
	   	    	 	  }
	   	    	 						
				} catch (IOException e) {
					wLogger.error("IOException: FileName:" + file.getName(), e);		
				} catch (IllegalStateException jEx) {
					wLogger.error("IllegalStateException: FileName: " + "\"" + file.getName() + "\"", jEx);	
			}
	  	    	catch (JsonSyntaxException jsyntax) {
	  	    		wLogger.error("JsonSyntaxException: FileName: " + "\"" + file.getName() + "\"", jsyntax);
	  	    	 }
				return jsonObject;	   	 
	    }
	    
	    // Test method: Constructs a tss command from a given JSON object and returns the corresponding command as String.
	    public String getTssCmdTest(JsonObject jObj, File file) {
	    	
	    	String cmd = null;
	    	try {
	    	//String fileName = jObj.get("file").getAsString();
	    	String fileName = file.getName();
	    	
	    	Path reqFile = App.writeOutDir.resolve(file.getName());
	    	
	    	//cmd = "/opt/tss/bin/tss " + "--recall --class=dc_atlas%ATLAS-DATA-AOD " + fileName + "  " + "/export/GridKatest/f01-129-131-e_wT_tst/data/in";
	    		    	    		 			 		
		 	cmd = "/opt/tss/bin/tss " + "--migrate --class=test_d " + reqFile + "  " + " /test/test/" + fileName + "  --description=/test/test/" + fileName; 
		 	wLogger.debug("Tss command: " + "\"" + cmd + "\"");			 
	
	    	
	    	}catch (NullPointerException nEx) {
	    		wLogger.error("JSON object is:" + jObj + " FileName: " + "\"" + file.getName() + "\"", nEx);
        	}
		 	return cmd;
		 	 	    	
	    }
	    
	    
	    // Constructs a tss command from a given JSON object and returns the corresponding command as String.
	    // tss cmd example: "/opt/tss/bin/tss --migrate --class=dc_atlas%ATLAS-DATA-AOD  /export/GridKatest/f01-129-131-e_wT_tst/data/out/00006B5FBF05ECA8452193F6916D4BA7E911  /dc_atlas/ATLAS-DATA-AOD/00006B5FBF05ECA8452193F6916D4BA7E911  --description=/pnfs/gridka.de/tapetests/test55.tar"
  	    public String getTssCmd(JsonObject jObj, File file) {
	    	
	    	String cmd = null;	    	
	    try {
	    	String action = jObj.get("action").getAsString();	 	    	
		 	String sClass = jObj.get("storage_class").getAsString();
		 	String storageClass = sClass.replace(':', '%');
		 	String [] classDef = storageClass.split("%");		 	 		 
		 	String path = jObj.get("path").getAsString();
		 			  	
		 	if (action.equals("migrate")) {	
		 		Path outFile = App.writeOutDir.resolve(file.getName());
		 		cmd = "/opt/tss/bin/tss " + "--" + action + "" + " --class=" + storageClass +  "  " + outFile +
		 				"  /" + classDef[0] + "/" + classDef[1] + "/" + file.getName()+ "  --description=" + path;
		 		wLogger.debug("Tss command: " + "\"" + cmd + "\"");		
		 				
		 		
		 	}
			}catch (NullPointerException nEx) {
				wLogger.error("JSON object is null: FileName: " + "\"" + file.getName() + "\"", nEx);
        	} 
	    	
		 	
		 	return cmd;
		 	 	    	
	    }
	    	
	    
	    // Removes a file from the "out" directory
	    public boolean removeFile (String fileName) throws IOException {	    	
	    	
	    	if (Files.deleteIfExists(App.writeOutDir.resolve(fileName))) {
		           return true;
		       }
	    	return false;
	    }
	    
	    // Checks if a process exists for a given PID.
		public boolean isStillAllive(Long pid) {	   
		   // String command = null;
		        //command = "ps -p " + pid + " | grep sh"; 
		    String command = "ps -p " + pid;  
		    return isProcessIdRunning(pid, command); 
		}
		// Executes the command and returns “true” or “false” if the corresponding PID exists or not.
		public boolean isProcessIdRunning(Long pid, String command) {
			// wLogger.debug("Checking command for pid: " + pid + "  Cmd: " + command);
		    try {
		        Process pr = Runtime.getRuntime().exec(command);      

		        InputStreamReader isReader = new InputStreamReader(pr.getInputStream());
		        BufferedReader bReader = new BufferedReader(isReader);
		        String strLine = null;
		        while ((strLine= bReader.readLine()) != null) {
		           if (strLine.contains("sh")) {
		                return true;
		            }
		        }
		        return false;
		    } catch (Exception ex) {
		    	wLogger.error("Exception: " + ex + "  cmd: " + command);
		        return false;
		    }
		}
		
		
		public static void callAScript(final File file, final String cmd) {
	    	
	    	 try {
	    		 
	    		 String[] str = new String[]{"/bin/sh", App.writeBashScript, cmd, file.getName()};	    			    		 
	    		 Process pScript = Runtime.getRuntime().exec(str); 
					
	    		 pScript.getErrorStream().close();
	    		 pScript.getInputStream().close();
	    		 pScript.getOutputStream().close(); 
					 
				 } catch (IOException e) {
					 wLogger.error("IOException: FileName:" + file.getName(), e);    				     
				 }
	    }
		
}

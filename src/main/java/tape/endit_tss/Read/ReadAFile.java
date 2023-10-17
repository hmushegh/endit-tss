package tape.endit_tss.Read;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;
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
 * The program does no wait for the completion of the created tss process.
 * 
 * Files are staged directly in the "in" directory of the ENDIT-Provider.  
 * 
 * After successfully staging the files, the ENDIT-Provider moves these 
 * files from its "in" directory to another location and removes the 
 * corresponding metadata file from its "request" directory.
 * 
 *   
 * @author Haykuhi Musheghyan, <haykuhi.musheghyan@kit.edu>, KIT
 * @year 2019
 */

public class ReadAFile {
	
		private static final Logger rLogger = Logger.getLogger(ReadAFile.class);
	
		 public ReadAFile () {					
			}
		
		 
		// Calls all necessary methods (extracts a JSON object from a metadata file, constructs the corresponding tss cmd and creates a tss process).  
		public void start (File file) throws InterruptedException, IOException, IllegalArgumentException, ExecutionException {
		       
	    	JsonObject jObj = getJsonFile(file);	
	    	if (!jObj.isJsonNull()) {
	    		String cmd = getTssCmd(jObj, file);		
	    		//String cmd = getTssCmdTest(jObj, file); // for the test only (w/o dcache)
	    		if (cmd != null) {			
	    			openAProcess(cmd,file);
	    		
	    		}else {
	    			rLogger.debug("Wrong tss command: " + cmd + " FileName: " + "\"" + file.getName() + "\"");
	    		}
	    	}
	    	else {
	    		rLogger.debug("JSON object is: " + jObj + " FileName: " + "\"" + file.getName() + "\"");
	    	}						
		}   
		
		
		// Extracts a JSON object from a metadata file and returns it. 
	    public JsonObject getJsonFile(File file) {
	    	
	      	 JsonObject jsonObject = null;
	  	    	  try {  
		    	 	  String fileContent = FileUtils.readFileToString(file, Charsets.UTF_8).trim();
		    	      jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();
		    		  if (!jsonObject.isJsonObject()){
		    			  rLogger.error("Error: Could not convert to JSON object:  FileName: " + "\"" + file.getName() + "\"");
	   	    	 	  }
	   	    	 						
				} catch (IOException e) {
					rLogger.error("IOException: FileName:" + file.getName(), e);	
				} catch (IllegalStateException jEx) {
	  	    		rLogger.error("IllegalStateException: FileName: " + "\"" + file.getName() + "\"", jEx);	
	  	    	}
	  	    	catch (JsonSyntaxException jsyntax) {
	  	    		rLogger.error("JsonSyntaxException: FileName: " + "\"" + file.getName() + "\"", jsyntax);	
	  	    	 }
				return jsonObject;	   	 
	    }
	    
	    
	    // Constructs a tss command from a given JSON object and returns the corresponding command as String.
	    // tss cmd example: "/opt/tss/bin/tss --recall --class=dc_atlas%ATLAS-DATA-AOD  /dc_atlas/ATLAS-DATA-AOD/00009E03E3FC44B048B49A7A6E64CE5A7DAA  /export/GridKatest/f01-129-131-e_sT_tst/data/in"
  	    public String getTssCmd(JsonObject jObj, File file) {
	    	
	    	String cmd = null;
	    	try {
	    		String action = jObj.get("action").getAsString();	 	    	
	    		String sClass = jObj.get("storage_class").getAsString();
	    		String path = jObj.get("hpss_path").getAsString();
	    		String storageClass = sClass.replace(':', '%');
	    		String [] classDef = storageClass.split("%");		 	 		 
		 			 	
	    		if (action.equals("recall") && path.isEmpty()) {		 		
	    			cmd = "/opt/tss/bin/tss " + "--" + action + "" + " --class=" + storageClass +  "  " 
		 				+ "/" + classDef[0] + "/" + classDef[1] + "/" + file.getName() + "  " + App.readInDir;
	    			rLogger.debug("Tss command: " + "\"" + cmd + "\"");
	    		}
	    	}catch (NullPointerException nEx) {
	    		rLogger.error("JSON object is: " + jObj + " FileName: " + "\"" + file.getName() + "\"", nEx);
        	} 
	    	
		 	return cmd;
		 	 	    	
	    }
	    
  	    
	    // Test method: Constructs a tss command from a given JSON object and returns the corresponding command as String.
	    public String getTssCmdTest(JsonObject jObj, File file) {
	    	
	    	String cmd = null;
	    	try {
	    	String fileName = jObj.get("file").getAsString();
	    	    		 			 		
		 	cmd = "/opt/tss/bin/tss " + "--recall --class=dc_atlas%ATLAS-DATA-AOD " + fileName + "  " + App.readInDir; 
		 	rLogger.debug("Tss command: " + "\"" + cmd + "\"");	
	    	
	    	}catch (NullPointerException nEx) {
	    		rLogger.error("JSON object is: " + jObj + " FileName: " + "\"" + file.getName() + "\"", nEx);
        	}
		 	return cmd;
		 	 	    	
	    }
	    
	    
	    // Creates a tss process and writes the successfully created PID to the metadata file.
	    // Stdin/out/err streams are closed.
	    public void openAProcess (final String cmd, final File file) throws InterruptedException, ExecutionException {    
	    	 
	    	Callable<String> callable = new Callable<String>() {
	 	    	
	  	    	public String call() throws Exception {
	  	    		 try {
	  	    	
	  	    			 Process p = Runtime.getRuntime().exec(cmd);
	  	    			
	                   	 if (p.isAlive()) {	
	            		    p.getErrorStream().close();
	            		    p.getInputStream().close();
	            		    p.getOutputStream().close(); 	            
	                   		rLogger.info("TSS process opened, file: " + "\"" + file.getName() + "\"");
	                   		Field f = p.getClass().getDeclaredField("pid");
		                    f.setAccessible(true);
		                    long pid = f.getLong(p);
	                   	    if (addMeta(file.getName(), "tss-pid", pid)){
		                    
		                    	return "true";
		                    	}
		                    else {
		                    	rLogger.error("Failure: Could not add a property ('tss-pid') to a JSON object: FileName: " + "\"" + file.getName() + "\"");	    
		                    	return "false";
		                    	}
		                    }	                	   
	                	  else {
	                		  rLogger.error("TSS process opening failed: FileName: " + "\"" + file.getName() + "\"");	                		  
		                   	 return "false";
	                	  }
	                
	  	    		 } catch (IOException e) {		  	    			
	  	    			 rLogger.error("IOException: FileName: " + "\"" + file.getName() + "\"", e);
	  	    			 return "false";
	  	    		}	  	    		 
	  		};
	  	       };	  	       	  		   
	    	// Calls the call() method.       
	  	     try {
				callable.call();
			} catch (Exception e) {
				rLogger.error("Exception: FileName: " + "\"" + file.getName() + "\"", e);				
			}	  	     
	  		  	  
	    }   
	    
	  	 
	    // Adds a property ('tss-pid') to a JSON object and overwrites the metadata file with a new JSON object.
	    public <T> boolean addMeta(String fileName, String key, Long value) { 	  
	    	Path requestFile = App.readRequestDir.resolve(fileName);
	      	JsonObject jsonObject = null;
		    	  try {  
		    	 	  String fileContent = FileUtils.readFileToString(requestFile.toFile(), Charsets.UTF_8).trim();
		    	 	  jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();
		    		  if (!jsonObject.isJsonObject()){
		    			  rLogger.error("Failure: Could not convert to JSON object: FileName: " + "\"" + fileName + "\"");
		    			 return false;
		    		}else {		    			 
		    			if (!Objects.isNull(value)) {		    				
	    				 jsonObject.addProperty(key, value);
	    			 	 FileUtils.writeStringToFile(requestFile.toFile(), jsonObject.toString(), Charsets.UTF_8);
		    			}		    			
		    			else {
		    				rLogger.error("Failure: Value is null: FileName: " + "\"" + fileName + "\"");
			    			return false;
		    			 }		    			
		    		}
	  	    	 						
				} catch (IOException e) {
					rLogger.error("IOException", e);
					return false;
				}
				return true;	   	 
	   }	  		
		   		
}

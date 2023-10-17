package tape.endit_tss;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
* The class used to read the parameters written in the "/etc/endit-tss.properties" file.
* 
*   
* @author Haykuhi Musheghyan, <haykuhi.musheghyan@kit.edu>, KIT
* @year 2019
*/

public class Config {
	private static final Logger logger = Logger.getLogger(Config.class);
	//private final URL config = Resources.getResource("/etc/endit-tss.properties");	   
	private Path writeOutDir;
	private Path writeRequestDir;
	private Path readInDir;
	private Path readRequestDir;
	public Properties prop;
	private String writeBashScript;
	private String action;
	private int activeRequests;
	
	// Loads parameters from the "config.properties" file
    public Config () throws IOException {
		 prop = new Properties();
		 
		 prop.load(new FileInputStream("/etc/endit-tss.properties"));
		 
    }
		 
    		public Path getReadRequestDir() {
				this.readRequestDir = FileSystems.getDefault().getPath(prop.getProperty("read.request.dir.path"));
				return this.readRequestDir;
			}
			
			
			public Path getReadInDir() {
				this.readInDir = FileSystems.getDefault().getPath(prop.getProperty("read.in.dir.path"));
				return this.readInDir;
			}
			
			public Path getWriteOutDir() {
				this.writeOutDir = FileSystems.getDefault().getPath(prop.getProperty("write.out.dir.path"));
				return this.writeOutDir;
			}
			
			public Path getWriteRequestDir() {
				this.writeRequestDir = FileSystems.getDefault().getPath(prop.getProperty("write.request.dir.path"));
				return this.writeRequestDir;
			}
					
				
			public String getWriteBashScript() {
				this.writeBashScript = prop.getProperty("write.bash.script");
				return this.writeBashScript;
			}
			
			public String getAction() {
				this.action = prop.getProperty("action");
				return this.action;
			}
			
			
			public int getActiveRequests() {
				this.activeRequests = Integer.parseInt(prop.getProperty("nr.active.requests"));
				return this.activeRequests;
			}
			
			
			
			
			

}

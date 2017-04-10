import BIT.highBIT.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class used to Instrument raytracer code. 
 * Obtains metrics from raytracer performance.
 * @author André
 *
 */
public class InstrumentRaytracer {
	private static int i_count = 0, b_count = 0, m_count = 0;
    
    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
    	System.out.println("Instrumenting...");
    	ArrayList<String> files = new ArrayList<String>();
    	
    	for(String dirName : argv) {	//For each directory in the arguments
    		if(Files.isDirectory(Paths.get(dirName))){	//If it is a directory 
    			File dir = new File(dirName);
    			for(String file : dir.list()) {
    				if(!Files.isDirectory(Paths.get(file))) {
    					if(!dirName.equals(".")) {
    						files.add(dir + System.getProperty("file.separator") + file);
    					}
    					else{
    						files.add(file);
    					}
    				}
    			}
    		}
    	}
        
        for (String infilename : files) {
        	System.out.println("Instrumenting: " + infilename);
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(infilename);
                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration<?> e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
					routine.addBefore("InstrumentRaytracer", "mcount", new Integer(1));                    
                    for (Enumeration<?> b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("InstrumentRaytracer", "count", new Integer(bb.size()));
                    }
                }
                ci.addAfter("InstrumentRaytracer", "printICount", ci.getClassName());
                ci.write(infilename);
            }
        }
    	System.out.println("Instrumenting Finished");
    }
    
    public static synchronized void printICount(String foo) {
        System.out.println(i_count + " instructions in " + b_count + " basic blocks were executed in " + m_count + " methods.");
    }

    public static synchronized void count(int incr) {
        i_count += incr;
        b_count++;
    }

    public static synchronized void mcount(int incr) {
		m_count++;
    }
}

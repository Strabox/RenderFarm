import BIT.highBIT.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import webserver.MultiThreadedWebServerMain;

/**
 * Class used to Instrument raytracer code. 
 * Obtains metrics from raytracer performance.
 * @author Andr�
 *
 */
public class InstrumentRaytracer {
	///private static int i_count = 0, b_count = 0, m_count = 0;
    
	
	
    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
    	System.out.println("Instrumenting...");
    	ArrayList<String> files = new ArrayList<String>();
    	//hash = new ConcurrentHashMap<Long,Measures>();
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
    
    public static void printICount(String foo) {
        Long threadID=Thread.currentThread().getId();
    	System.out.println("Input:"+ "f:" + MultiThreadedWebServerMain.hash.get(threadID).getFile()+ " sc:" + MultiThreadedWebServerMain.hash.get(threadID).getScolumns() + " sr:" + MultiThreadedWebServerMain.hash.get(threadID).getSrows()+" wc:" + MultiThreadedWebServerMain.hash.get(threadID).getWidth()+ " wr:" +MultiThreadedWebServerMain.hash.get(threadID).getHeight() + " coff:" + MultiThreadedWebServerMain.hash.get(threadID).getColumnOffset()+ " roff:"+ MultiThreadedWebServerMain.hash.get(threadID).getRowOffset()+ " "+MultiThreadedWebServerMain.hash.get(threadID).getI_count() + " instructions in " + MultiThreadedWebServerMain.hash.get(threadID).getB_count()+ " basic blocks were executed in " + MultiThreadedWebServerMain.hash.get(threadID).getM_count() + " methods.");
    	MultiThreadedWebServerMain.hash.get(threadID).reset();
    }

    public static void count(int incr) {
    	Long threadID=Thread.currentThread().getId();
    	MultiThreadedWebServerMain.hash.get(threadID).setI_count(MultiThreadedWebServerMain.hash.get(threadID).getI_count()+incr);
    	MultiThreadedWebServerMain.hash.get(threadID).setB_count(MultiThreadedWebServerMain.hash.get(threadID).getB_count()+1);
    }

    public static void mcount(int incr) {
    	Long threadID=Thread.currentThread().getId();
    	MultiThreadedWebServerMain.hash.get(threadID).setM_count(MultiThreadedWebServerMain.hash.get(threadID).getM_count()+1);
    }
    
}

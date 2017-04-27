import BIT.highBIT.*;
import metrics.Measures;
import metrics.Metric;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import webserver.MultiThreadedWebServerMain;

/**
 * Class used to Instrument raytracer code. 
 * Obtains metrics from raytracer performance.
 * @author Andre
 *
 */
public class InstrumentRaytracer {  
	
	private static final String METRICS_LOG_FILENAME = "output.txt";
	
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
                // Loop through all the routines
                for (Enumeration<?> e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
					InstructionArray instructions = routine.getInstructionArray();
					// Loop through all the instructions
					for (Enumeration<?> instrs = instructions.elements(); instrs.hasMoreElements(); ) {
						Instruction instr = (Instruction) instrs.nextElement();
						int opcode=instr.getOpcode();
						short instr_type = InstructionTable.InstructionTypeTable[opcode];
						if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
							instr.addBefore("InstrumentRaytracer", "LSCount", new Integer(0));
						}
						else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
							instr.addBefore("InstrumentRaytracer", "LSCount", new Integer(1));
						}
						
					}
					
                    for (Enumeration<?> b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("InstrumentRaytracer", "blocksCount", new Integer(bb.size()));
                    }
                }
                ci.addAfter("InstrumentRaytracer", "requestFinished", ci.getClassName());
                ci.write(infilename);
            }
        }
    	System.out.println("Instrumenting Finished");
    }
    
    public static void requestFinished(String foo) throws IOException {
        Long threadID = Thread.currentThread().getId();
    	Metric requestMetrics = MultiThreadedWebServerMain.metricsGatherer.get(threadID);
    	
    	System.out.println(requestMetrics);
    	
		FileWriter fw =  new FileWriter(METRICS_LOG_FILENAME,true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		if(new File(METRICS_LOG_FILENAME).length() != 0)
			bw.newLine();
		
		bw.write("Input_file: " + requestMetrics.getFileName());
		bw.newLine();
		bw.write("X: " + requestMetrics.getNormalizedWindow().getX());
		bw.newLine();
		bw.write("Y: "+ requestMetrics.getNormalizedWindow().getY());
		bw.newLine();
		bw.write("Window_width: "+ requestMetrics.getNormalizedWindow().getWidth());
		bw.newLine();
		bw.write("Window_height: " + requestMetrics.getNormalizedWindow().getHeight());
		bw.newLine();
		bw.write("Total_pixels_rendered: " + requestMetrics.getTotalPixelsRendered());
		bw.newLine();
		bw.write("Basic blocks: " + requestMetrics.getMeasures().getBasicBlockCount());
		bw.newLine();
		bw.write("Loads: " + requestMetrics.getMeasures().getLoadcount());
		bw.newLine();
		bw.write("Stores: " + requestMetrics.getMeasures().getStorecount());
		bw.newLine();
		bw.flush();
		bw.close();
		fw.close();
		requestMetrics.reset();
    }

    public static void blocksCount(int incr) {
    	Long threadID = Thread.currentThread().getId();
    	Measures measures = MultiThreadedWebServerMain.metricsGatherer.get(threadID).getMeasures();
    	measures.incrementBasiBlockCount();
    }

    public static void LSCount(int type) {
		Long threadID = Thread.currentThread().getId();
		Measures measures = MultiThreadedWebServerMain.metricsGatherer.get(threadID).getMeasures();
		if (type == 0)
			measures.incrementLoadCount();
		else
			measures.incrementStoreCount();;
	}
    
}

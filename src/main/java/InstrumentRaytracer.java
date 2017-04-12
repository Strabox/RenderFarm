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
					InstructionArray instructions = routine.getInstructionArray();
					  
					for (Enumeration<?> instrs = instructions.elements(); instrs.hasMoreElements(); ) {
						Instruction instr = (Instruction) instrs.nextElement();
						int opcode=instr.getOpcode();
						if (opcode == InstructionTable.getfield)
							instr.addBefore("InstrumentRaytracer", "LSFieldCount", new Integer(0));
						else if (opcode == InstructionTable.putfield)
							instr.addBefore("InstrumentRaytracer", "LSFieldCount", new Integer(1));
						else {
							short instr_type = InstructionTable.InstructionTypeTable[opcode];
							if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
								instr.addBefore("InstrumentRaytracer", "LSCount", new Integer(0));
							}
							else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
								instr.addBefore("InstrumentRaytracer", "LSCount", new Integer(1));
							}
						}
						if ((opcode==InstructionTable.NEW) ||
							(opcode==InstructionTable.newarray) ||
							(opcode==InstructionTable.anewarray) ||
							(opcode==InstructionTable.multianewarray)) {
							instr.addBefore("InstrumentRaytracer", "allocCount", new Integer(opcode));
						}
					}
					
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
    	System.out.println("Input:"+ "f:" + MultiThreadedWebServerMain.hash.get(threadID).getFile()+ " sc:" + MultiThreadedWebServerMain.hash.get(threadID).getScolumns() + " sr:" + MultiThreadedWebServerMain.hash.get(threadID).getSrows()+" wc:" + MultiThreadedWebServerMain.hash.get(threadID).getWidth()+ " wr:" +MultiThreadedWebServerMain.hash.get(threadID).getHeight() + " coff:" + MultiThreadedWebServerMain.hash.get(threadID).getColumnOffset()+ " roff:"+ MultiThreadedWebServerMain.hash.get(threadID).getRowOffset()+ " "+MultiThreadedWebServerMain.hash.get(threadID).getI_count() + " instructions in " + MultiThreadedWebServerMain.hash.get(threadID).getB_count()+ " basic blocks were executed in " + MultiThreadedWebServerMain.hash.get(threadID).getM_count() + " methods"
    				+" with "+MultiThreadedWebServerMain.hash.get(threadID).getNewcount()+" newcounts and "+MultiThreadedWebServerMain.hash.get(threadID).getAnewarraycount()+" anewarraycount and " + MultiThreadedWebServerMain.hash.get(threadID).getMultianewarraycount()+" multianewarraycount");
    	System.out.println("FieldLoads: "+ MultiThreadedWebServerMain.hash.get(threadID).getFieldloadcount());
    	System.out.println("FieldStores: "+ MultiThreadedWebServerMain.hash.get(threadID).getFieldstorecount());
    	System.out.println("Loads: "+ MultiThreadedWebServerMain.hash.get(threadID).getLoadcount());
    	System.out.println("Stores: "+ MultiThreadedWebServerMain.hash.get(threadID).getStorecount());
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
    
    public static void allocCount(int type)
	{
		switch(type) {
		case InstructionTable.NEW:
			Long threadID=Thread.currentThread().getId();
	    	MultiThreadedWebServerMain.hash.get(threadID).setNewcount(MultiThreadedWebServerMain.hash.get(threadID).getNewcount()+1);
			break;
		case InstructionTable.newarray:
			threadID=Thread.currentThread().getId();
	    	MultiThreadedWebServerMain.hash.get(threadID).setNewarraycount(MultiThreadedWebServerMain.hash.get(threadID).getNewarraycount()+1);
			break;
		case InstructionTable.anewarray:
			threadID=Thread.currentThread().getId();
	    	MultiThreadedWebServerMain.hash.get(threadID).setAnewarraycount(MultiThreadedWebServerMain.hash.get(threadID).getAnewarraycount()+1);
			break;
		case InstructionTable.multianewarray:
			threadID=Thread.currentThread().getId();
	    	MultiThreadedWebServerMain.hash.get(threadID).setMultianewarraycount(MultiThreadedWebServerMain.hash.get(threadID).getMultianewarraycount()+1);
			break;
		}
	}
    
    public static void LSFieldCount(int type) 
	{
    	Long threadID=Thread.currentThread().getId();
		if (type == 0)	
    	MultiThreadedWebServerMain.hash.get(threadID).setFieldloadcount(MultiThreadedWebServerMain.hash.get(threadID).getFieldloadcount()+1);
		else
			MultiThreadedWebServerMain.hash.get(threadID).setFieldstorecount(MultiThreadedWebServerMain.hash.get(threadID).getFieldstorecount()+1);
	}

public static void LSCount(int type) 
	{
		Long threadID=Thread.currentThread().getId();
		if (type == 0)
			MultiThreadedWebServerMain.hash.get(threadID).setLoadcount(MultiThreadedWebServerMain.hash.get(threadID).getLoadcount()+1);
		else
			MultiThreadedWebServerMain.hash.get(threadID).setStorecount(MultiThreadedWebServerMain.hash.get(threadID).getStorecount()+1);
	}
    
}

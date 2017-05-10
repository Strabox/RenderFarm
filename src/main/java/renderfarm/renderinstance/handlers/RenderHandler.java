package renderfarm.renderinstance.handlers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import raytracer.Main;
import renderfarm.renderinstance.MultiThreadedWebServerMain;
import renderfarm.util.Metric;
import renderfarm.util.NormalizedWindow;
import renderfarm.util.RenderFarmUtil;

/**
 * Handler that receive requests to handle rendering requests
 * @author Andre
 *
 */
public class RenderHandler implements HttpHandler {
	
	private final static String OS = System.getProperty("os.name").toLowerCase();

	
    @Override
    public void handle(HttpExchange t) throws IOException {
    	System.out.println("Thread ID: " + Thread.currentThread().getId());
    	String response;
        Map<String,String> paramMap = RenderFarmUtil.getQueryMap(t.getRequestURI().getQuery());
        OutputStream out = t.getResponseBody();
        
        Long threadID = Thread.currentThread().getId();
        if(MultiThreadedWebServerMain.metricsGatherer.get(threadID) == null){
        	MultiThreadedWebServerMain.initMeasure(threadID);
        }
        
        if(paramMap.containsKey("f") && paramMap.containsKey("sc") && paramMap.containsKey("sr") 
        		&& paramMap.containsKey("wc") && paramMap.containsKey("wr")
        		&& paramMap.containsKey("coff") && paramMap.containsKey("roff")) {
        	String inputFileName = paramMap.get("f");
        	if(OS.indexOf("win") >= 0) {
        		inputFileName = "win-" + inputFileName;
        	}
        	Long sceneHeight = Long.parseLong(paramMap.get("sr"));
        	Long sceneWidth = Long.parseLong(paramMap.get("sc"));
        	Long windowWidth = Long.parseLong(paramMap.get("wc"));
        	Long windowHeight = Long.parseLong(paramMap.get("wr"));
        	Long collumnOffset = Long.parseLong(paramMap.get("coff"));
        	Long rowOffset = Long.parseLong(paramMap.get("roff"));

        	String[] args = { inputFileName, "out" + Thread.currentThread().getId() + ".bmp", paramMap.get("sc"), paramMap.get("sr"),
        			paramMap.get("wc"), paramMap.get("wr"), paramMap.get("coff"), paramMap.get("roff")};
        	System.out.println("INfile:" + args[0] + ";OUTfile:" + args[1] + ";sc:" + args[2] + ";sr:" + 
        			args[3] + ";wc:" + args[4] + ";wr:" + args[5] + ";coff:" + args[6] + ";roff:" + args[7]+ ";");
			try {
				Metric metric = MultiThreadedWebServerMain.metricsGatherer.get(threadID);
				metric.setFileName(paramMap.get("f"));
				metric.setTotalPixelsRendered(sceneHeight * sceneWidth);
				metric.setNormalizedWindow(NormalizedWindow
						.BuildNormalizedWindow(sceneWidth, sceneHeight, windowWidth, windowHeight, collumnOffset, rowOffset));
				
				Main.main(args);	// Executing the raytracing requested.
				
				response = "Ok request: " + t.getRequestURI().getQuery();
				File image = new File("out" + Thread.currentThread().getId() + ".bmp");
				t.sendResponseHeaders(RenderFarmUtil.HTTP_OK, image.length());
				Files.copy(image.toPath(), out);
			} catch(IOException e) {
				response = "No test file to start rendering";
				t.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
				e.printStackTrace();
			} catch (InterruptedException e) {
				response = "Problem rendering";
				t.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
				e.printStackTrace();
			} catch(RuntimeException e) {
				response = "Serious problem rendering (Probably problem with input files UNIX/WIN format)";
				t.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
				e.printStackTrace();
			}
        }
        else {	//Invalid Request - Argument(s) Missing
        	response = "Bad request: " + t.getRequestURI().getQuery();
        	t.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
        	out.write(response.getBytes());
        }
        out.close();
    }
}

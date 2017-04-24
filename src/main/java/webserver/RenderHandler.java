package webserver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import metrics.Metric;
import metrics.NormalizedWindow;
import raytracer.Main;

public class RenderHandler implements HttpHandler {
	
	private final static String OS = System.getProperty("os.name").toLowerCase();
	
	/**
	 * Used to parse request parameters
	 * @param query
	 * @return Map with HttpURI <parameter,value>
	 */
	public Map<String, String> getQueryMap(String query)
	{
	    String[] params = query.split("&");
	    Map<String, String> map = new HashMap<String, String>();
	    for (String param : params)
	    {
	        String name = param.split("=")[0];
	        String value = param.split("=")[1];
	        map.put(name, value);
	    }
	    return map;
	}
	
    @Override
    public void handle(HttpExchange t) throws IOException {
    	System.out.println("Thread ID: " + Thread.currentThread().getId());
    	String response;
        Map<String,String> paramMap = getQueryMap(t.getRequestURI().getQuery());
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
				metric.setTotalPixelsRendered(Long.parseLong(paramMap.get("wc")) * Long.parseLong(paramMap.get("wr")));
				metric.setNormalizedWindow(NormalizedWindow
						.BuildNormalizedWindow(sceneWidth, sceneHeight, windowWidth, windowHeight, collumnOffset, rowOffset));
				
				Main.main(args);	// Executing the raytracing requested.
				
				response = "Ok request: " + t.getRequestURI().getQuery();
				File image = new File("out" + Thread.currentThread().getId() + ".bmp");
				t.sendResponseHeaders(200, image.length());
				Files.copy(image.toPath(), out);
			} catch(IOException e) {
				response = "No test file to start rendering";
				t.sendResponseHeaders(200, response.length());
				out.write(response.getBytes());
				e.printStackTrace();
			} catch (InterruptedException e) {
				response = "Problem rendering";
				t.sendResponseHeaders(200, response.length());
				out.write(response.getBytes());
				e.printStackTrace();
			} catch(RuntimeException e) {
				response = "Serious problem rendering";
				t.sendResponseHeaders(200, response.length());
				out.write(response.getBytes());
				e.printStackTrace();
			}
        }
        else {	//Invalid Request - Argument(s) Missing
        	response = "Bad requestttaaaaaaaaaaaa: " + t.getRequestURI().getQuery();
        	t.sendResponseHeaders(200, response.length());
        	out.write(response.getBytes());
        }
        out.close();
    }
}

package renderfarm.loadbalancer.handlers;

import java.io.IOException;
import java.net.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InvalidRenderingRequest;
import renderfarm.loadbalancer.exceptions.MaximumRedirectRetriesReached;
import renderfarm.util.NormalizedWindow;
import renderfarm.util.RenderFarmUtil;
import renderfarm.util.SystemConfiguration;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Load balancer handler that receives the requests from the users and
 * redirect it to our farm.
 * @author Andre
 *
 */
public class RequestHandler implements HttpHandler {

	private static final int BUFFER_SIZE = 1024;
	
	/**
	 * Maximum time tolerate to connect to an instance.
	 */
	private static final int CONNECTION_TIMEOUT = 10000;
	
	/**
	 * Time interval between each retry in a request 
	 */
	private static final int TIME_INTERVAL_TO_RETRY = 500;
	
	/**
	 * Maximum number of tries that load balancer will do.
	 * NOTE: This will only occur if many Render Instances fail during the
	 * requests.
	 */
	private static final int MAXIMUM_NUMBER_OF_RETRIES = 4;
	
	/**
	 * Number of tries done per receiving thread <ThreadId,Retries>.
	 */
	private ConcurrentHashMap<Long,Integer> numberOfRetries;			//Thread safe
	
	/**
	 * Represents for each thread what instance are trying to contact.
	 */
	public ConcurrentHashMap<Long,String> threadInstance;				//Thread safe
	
	/**
	 * Render farm instance manager.
	 */
	private final RenderFarmInstanceManager instanceManager;			//Thread safe
	
	public RequestHandler(RenderFarmInstanceManager instanceManager) {
		this.numberOfRetries = new ConcurrentHashMap<Long,Integer>();
		this.threadInstance = new ConcurrentHashMap<Long,String>();
		this.instanceManager = instanceManager;
	}
	
	@Override
	public void handle(HttpExchange http) {
		final String requestParams = http.getRequestURI().getQuery();
		final Map<String,String> paramMap = RenderFarmUtil.getQueryMap(requestParams);
		final Request request;
		final OutputStream out = http.getResponseBody();
		System.out.println(instanceManager);
		try{
			if(!paramMap.containsKey("f") || !paramMap.containsKey("sc") || !paramMap.containsKey("sr") 
	        		|| !paramMap.containsKey("wc") || !paramMap.containsKey("wr")
	        		|| !paramMap.containsKey("coff") || !paramMap.containsKey("roff")) {
			   throw new InvalidRenderingRequest();
			}
			Long sceneHeight = Long.parseLong(paramMap.get("sr"));
			Long sceneWidth = Long.parseLong(paramMap.get("sc"));
			Long windowWidth = Long.parseLong(paramMap.get("wc"));
			Long windowHeight = Long.parseLong(paramMap.get("wr"));
			Long collumnOffset = Long.parseLong(paramMap.get("coff"));
			Long rowOffset = Long.parseLong(paramMap.get("roff"));
			
			request = new Request(paramMap.get("f") ,
					NormalizedWindow.BuildNormalizedWindow(sceneWidth, sceneHeight, windowWidth, windowHeight, collumnOffset, rowOffset)
					,windowWidth * windowHeight);
			
			redirectRequest(http, requestParams, request);
			
			System.out.println("[Handler]Request processed with success");
		} catch(InvalidRenderingRequest e) {
			String response = "Bad request: " + http.getRequestURI().getQuery();
			try{ 
				http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
			} catch(IOException e1) {
				e.printStackTrace();
			}
			System.out.println("[Handler]Request with wrong format");
		} catch(MaximumRedirectRetriesReached e) {
			String response = "Retry later: " + http.getRequestURI().getQuery();
			try{ 
				http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
			} catch(IOException e1) {
				e.printStackTrace();
			}
			System.out.println("[Handler]System is collapsing retry later");
		} catch(Exception e) {
			e.printStackTrace();
			String response = "Something wrong happened, retry later: " + http.getRequestURI().getQuery();
			try{ 
				http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
			} catch(IOException e1) {
				e.printStackTrace();
			}
			System.out.println("[Handler]Something wrong happened");
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			resetThreadNumberOfRetries();	//Reset the request retries to next request.
		}
	}
	
	/**
	 * Redirect the request to a render farm instance.
	 * @param http
	 * @param requestParams
	 * @param request
	 * @throws MaximumRedirectRetriesReached
	 */
	private void redirectRequest(HttpExchange http, String requestParams, Request request) throws MaximumRedirectRetriesReached {
		final OutputStream out = http.getResponseBody();
		InputStream is = null;
		String handlerInstanceIP = null;
		try {
			System.out.println("[Handler]Looking for best instance..");
			handlerInstanceIP = instanceManager.getHandlerInstanceIP(request);
			URL url = new URL("http",handlerInstanceIP,SystemConfiguration.RENDER_INSTANCE_PORT,"/r.html?" + requestParams);
			System.out.println("[Handler]Redirecting number " + getThreadNumberOfRetries() + " of request to instance URL: " + url.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.connect();
			System.out.println("[Handler]Getting inputstream...");
			is = connection.getInputStream();
			http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, 0);
			byte[] buffer = new byte[BUFFER_SIZE]; // Adjust if you want
			int bytesRead;
			System.out.println("[Handler]Waiting for instance reply with image...");
			while ((bytesRead = is.read(buffer)) != -1)
			{
	    		out.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {	//Problem Rendering (Probably instance DIED)
			e.printStackTrace();
			System.out.println("[Handler]Instance died going to redirect to other instance");
			try {
				if(is != null) {
					is.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(request != null && handlerInstanceIP != null) {
				//Remove the request from the instance structure since it is done.
				instanceManager.removeRequestFromInstance(handlerInstanceIP, request);
			}
			if(getThreadNumberOfRetries() == MAXIMUM_NUMBER_OF_RETRIES) {
				throw new MaximumRedirectRetriesReached();
			}
			incrementNumberOfRetries();
			try {
				Thread.sleep(TIME_INTERVAL_TO_RETRY);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			redirectRequest(http, requestParams, request);	//Recursive Maximum Depth = MAXIMUM_NUMBER_OF_RETRIES
			return;
		} finally {	//Set all the resources free (Request processed with SUCCESS)
			try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(request != null && handlerInstanceIP != null) {
				//Remove the request from the instance structure since it is done.
				instanceManager.removeRequestFromInstance(handlerInstanceIP, request);
			}
		}
	}

	private int getThreadNumberOfRetries() {
		Integer noR = numberOfRetries.get(Thread.currentThread().getId());
		if(noR == null) {
			return 0;
		} else {
			return noR;
		}
	}

	private void incrementNumberOfRetries() {
		numberOfRetries.put(Thread.currentThread().getId(), getThreadNumberOfRetries() + 1);
	}
	
	private void resetThreadNumberOfRetries() {
		numberOfRetries.put(Thread.currentThread().getId(), 0);
	}
	
}

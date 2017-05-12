package renderfarm.loadbalancer.handlers;

import java.io.IOException;
import java.net.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import renderfarm.loadbalancer.KeepAliveThread;
import renderfarm.loadbalancer.RenderFarmInstance;
import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.loadbalancer.Request;
import renderfarm.loadbalancer.exceptions.InvalidRenderingRequestException;
import renderfarm.loadbalancer.exceptions.MaximumRedirectRetriesReachedException;
import renderfarm.loadbalancer.exceptions.NoInstancesToHandleRequestException;
import renderfarm.loadbalancer.exceptions.RedirectFailedException;
import renderfarm.util.NormalizedWindow;
import renderfarm.util.RenderFarmUtil;
import renderfarm.util.SystemConfiguration;

import java.net.URL;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Load balancer handler that receives the requests from the users and
 * redirect it to our farm.
 * @author Andre
 *
 */
public class RequestHandler implements HttpHandler {

	/**
	 * Buffer size to get the render instance image reply.
	 */
	private static final int BUFFER_SIZE = 2048;
	
	/**
	 * Maximum time tolerate to connect to a render instance.
	 */
	private static final int CONNECTION_TIMEOUT = 10000;
	
	/**
	 * Maximum time for a rendering request.
	 */
	private static final int MAXIMUM_TIME_FOR_RENDERING =  (3 * (60 * 60) * 1000);
	
	/**
	 * Time interval between each retry in a request 
	 */
	private static final int TIME_INTERVAL_TO_RETRY = 1000;
	
	/**
	 * Maximum number of tries that load balancer will do.
	 * NOTE: This maximum will only occur if many Render Instances
	 * fail during the requests.
	 */
	private static final int MAXIMUM_NUMBER_OF_RETRIES = 3;

	/**
	 * Render farm instance manager.
	 */
	private final RenderFarmInstanceManager instanceManager;			//Thread safe
	
	
	public RequestHandler(RenderFarmInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}
	
	@Override
	public void handle(HttpExchange http) {
		final String requestParams = http.getRequestURI().getQuery();
		final Map<String,String> paramMap = RenderFarmUtil.getQueryMap(requestParams);
		final Request request;
		final OutputStream out = http.getResponseBody();
		try{
			if(!paramMap.containsKey("f") || !paramMap.containsKey("sc") || !paramMap.containsKey("sr") 
	        		|| !paramMap.containsKey("wc") || !paramMap.containsKey("wr")
	        		|| !paramMap.containsKey("coff") || !paramMap.containsKey("roff")) {
			   throw new InvalidRenderingRequestException();
			}

			Long sceneHeight = Long.parseLong(paramMap.get("sr"));
			Long sceneWidth = Long.parseLong(paramMap.get("sc"));
			Long windowWidth = Long.parseLong(paramMap.get("wc"));
			Long windowHeight = Long.parseLong(paramMap.get("wr"));
			Long collumnOffset = Long.parseLong(paramMap.get("coff"));
			Long rowOffset = Long.parseLong(paramMap.get("roff"));
			
			request = new Request(paramMap.get("f") ,
					NormalizedWindow.BuildNormalizedWindow(sceneWidth, sceneHeight, windowWidth, windowHeight, collumnOffset, rowOffset)
					,sceneHeight * sceneWidth,windowWidth * windowHeight);
			
			for(int i = 0; i < MAXIMUM_NUMBER_OF_RETRIES; i++) {
				try {
					redirectRequestToRenderFarmInstance(http, requestParams, request);
					break;
				} catch(RedirectFailedException e) {
					if((i + 1) == MAXIMUM_NUMBER_OF_RETRIES) {
						throw new MaximumRedirectRetriesReachedException();
					}
				}
				try {
					Thread.sleep(TIME_INTERVAL_TO_RETRY);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			
			System.out.println("[Handler]Rendering request processed with success");
		} catch(InvalidRenderingRequestException e) {
			System.out.println("[Handler]Request with wrong format");
			String response = "Bad request: " + http.getRequestURI().getQuery();
			try{ 
				http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
			} catch(IOException e1) {
				e.printStackTrace();
			}
		} catch(MaximumRedirectRetriesReachedException e) {
			System.out.println("[Handler]Maximum retries reached");
			String response = "Retry later maximum tries reached: " + http.getRequestURI().getQuery();
			try{ 
				http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
			} catch(IOException e1) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			System.out.println("[Handler]Something wrong happened");
			e.printStackTrace();
			String response = "Something wrong happened, retry later: " + http.getRequestURI().getQuery();
			try{ 
				http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
				out.write(response.getBytes());
			} catch(IOException e1) {
				e.printStackTrace();
			}
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Redirect the request to a render farm instance.
	 * @param http
	 * @param requestParams
	 * @param request
	 * @throws RedirectFailedException 
	 * @throws MaximumRedirectRetriesReachedException
	 * @throws InvalidRenderingRequestException 
	 */
	private void redirectRequestToRenderFarmInstance(HttpExchange http, String requestParams, Request request) 
			throws RedirectFailedException {
		final OutputStream out = http.getResponseBody();
		int bytesRead;
		boolean retry = false;
		InputStream in = null;
		//KeepAliveThread keepAliveThread = null;
		RenderFarmInstance selectedInstance = null;
		try {
			System.out.println("[Handler]Looking for best instance..");
			selectedInstance = instanceManager.getHandlerInstanceIP(request);
			URL url = new URL("http",selectedInstance.getIp(),SystemConfiguration.RENDER_INSTANCE_PORT,"/r.html?" + requestParams);
			System.out.println("[Handler]Redirecting number of request to instance URL: " + url.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(MAXIMUM_TIME_FOR_RENDERING);
			connection.connect();
			//keepAliveThread = new KeepAliveThread(connection, selectedInstance.getIp());
			//keepAliveThread.start();
			System.out.println("[Handler]Getting response code...");
			System.out.println("[Handler]Getting input stream...");
			in = connection.getInputStream();
			//keepAliveThread.terminate();
			http.sendResponseHeaders(RenderFarmUtil.HTTP_OK, 0);
			byte[] buffer = new byte[BUFFER_SIZE];
			System.out.println("[Handler]Waiting for instance reply with image...");
			while ((bytesRead = in.read(buffer)) != -1)
			{
	    		out.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {	//Problem Rendering (Probably instance DIED) going to redirect
			e.printStackTrace();
			System.out.println("[Handler]Instance \"probably\" died, going to redirect to other instance");
			retry = true;
		} catch(NoInstancesToHandleRequestException e) {
			retry = true;
			// WEIRD CASE
			// TODO what we do if there are no instances to handle the request ?
		} finally {	//Set all the resources free (Request processed with SUCCESS)
			//if(keepAliveThread != null) {
			//	keepAliveThread.terminate();
			//}
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(request != null && selectedInstance != null) {
				//Remove the request from the instance structure.
				instanceManager.removeRequestFromInstance(selectedInstance, request);
			}
			if(retry) {
				throw new RedirectFailedException();
			}
		}
	}

	
}

package loadbalancer;

import java.io.IOException;
import java.net.*;
import java.io.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import loadbalancer.LoadBalancerMain;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;


public class RequestHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange arg0) throws IOException {
		String params= (String)arg0.getRequestURI().getQuery();
		OutputStream out = arg0.getResponseBody();
		String ip = LoadBalancerMain.instanceManager.getAvailableInstance();//LoadBalancerMain.instanceManager.getInstanceIp(LoadBalancerMain.instanceManager.getFirstInstanceId());
		URL url = new URL("http",ip,8000,"/r.html?"+params);
		System.out.println("[Handler] " + url.toString());
		try{
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
   			InputStream is = connection.getInputStream();
    		arg0.sendResponseHeaders(200, 0);
			byte[] buffer = new byte[1024]; // Adjust if you want
    		int bytesRead;
    		while ((bytesRead = is.read(buffer)) != -1)
    		{
        		out.write(buffer, 0, bytesRead);
   			}
			is.close();
			out.close();
			
		}	
		catch (Exception e){
			String response = "No available machines for rendering try later...";
			arg0.sendResponseHeaders(200, response.length());
			out.write(response.getBytes());
			e.printStackTrace();
			out.close();
		}
	}

}

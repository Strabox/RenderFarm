package webserver;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler for health check request from the load balancer.
 * @author André
 *
 */
public class HealthCheckHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		OutputStream out = t.getResponseBody();
		String response = "I'm Fine";
    	t.sendResponseHeaders(200, response.length());
    	out.write(response.getBytes());
    	out.close();
	}

}

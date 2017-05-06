package renderfarm.loadbalancer.handlers;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import renderfarm.loadbalancer.RenderFarmInstanceManager;
import renderfarm.util.RenderFarmUtil;

public class FarmStatusHandler implements HttpHandler {

	
	private final RenderFarmInstanceManager rfim;
	
	public FarmStatusHandler(RenderFarmInstanceManager rfim) {
		this.rfim = rfim;
	}
	
	@Override
	public void handle(HttpExchange t) throws IOException {
		String response = rfim.toString();
		OutputStream out = t.getResponseBody();
		t.sendResponseHeaders(RenderFarmUtil.HTTP_OK, response.length());
		out.write(response.getBytes());
		out.close();
	}

}

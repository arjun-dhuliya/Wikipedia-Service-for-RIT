import java.io.IOException;
import java.net.Socket;

public class Node {
	public static int port = 8080;
	public static String dbIp = "129.21.37.67";
	public static int dbPort = 27017;
	public static void main(String[] args) {
//		// start http server
//		SimpleHttpServer httpServer = new SimpleHttpServer();
//		httpServer.Start(port);
		
		// start https server
		SimpleHttpServer httpsServer = new SimpleHttpServer();
		httpsServer.Start(port);

	}
}

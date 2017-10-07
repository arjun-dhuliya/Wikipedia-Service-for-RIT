import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    private HttpServer server;

    public void Start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("server started at " + port);
            server.createContext("/index", new Handlers.RootHandler());
            server.createContext("/get", new Handlers.GetHandler());
            server.createContext("/edit", new Handlers.EditHandler());
            server.createContext("/add", new Handlers.AddHandler());
            server.createContext("/create", new Handlers.CreateHandler());
            server.createContext("/submit", new Handlers.SubmitHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Stop() {
        server.stop(0);
        System.out.println("server stopped");
    }
}

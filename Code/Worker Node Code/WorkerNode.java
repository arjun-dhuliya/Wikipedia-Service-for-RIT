import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WorkerNode {
    private static final int port = 8080;
    public static final ArrayList<String> dbIp;
    public static int TOTAL_DB;
    public static final ArrayList<Integer> dbPort;
    public static String ownIp;

    static {
        dbIp = new ArrayList<>();
        dbPort = new ArrayList<>();
        dbIp.add("129.21.37.67"); //argonaut
        dbPort.add(28000); // port of argonaut
        dbIp.add("129.21.37.69"); //argus
        dbPort.add(28000); // port of argus

    }

    public static void main(String[] args) {
        // start http server
        try {
            ownIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        SimpleHttpServer httpServer = new SimpleHttpServer();
        httpServer.Start(port);
//        String monitorIp = "172.17.0.16";
        String monitorIp = Config.VALID_IPS.get(0);
        int port = 7777;
        try {
            Socket socket = new Socket(monitorIp, port);
//            byte[] bytes = "Hello Monitor!!!!".getBytes();
//            Config.sendBytes(bytes, socket);
//            bytes = Config.readBytes(socket);
//            System.out.println("Received "+ new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void sendHealth(Socket socket){
        while (true){

        }
    }
}

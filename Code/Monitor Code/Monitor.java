/*
 * @author Arjun Dhuliya
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Monitor {

    private static final int TOTAL_SERVERS;
    private static final ArrayList<String> VALID_IPS;
    static final int PORT_NUMBER = 6543;
    private static final HashMap<String, Integer> IP_TO_INDEX;
    private static final ArrayList<Socket> slave_sockets;
    private static ServerSocket Monitor_socket;
    private static final int total_slaves;
    private static final HashMap<String, Integer> Slave_Health;
    private static final ArrayList<Thread> slave_threads;
    private static int slave_id = -1;

    private static int lastIndexUsed = -1;

    /*
     static constructor
     */
    static {
        total_slaves = 2;
        slave_sockets = new ArrayList<>();

        slave_threads = new ArrayList<>();
        Slave_Health = new HashMap<>();

        VALID_IPS = new ArrayList<>();
        // Listed docker machines --> domino argus cyclops midas tiresias carya yes argonaut
        VALID_IPS.add("129.21.37.28");     //yes -> index 0
        VALID_IPS.add("129.21.37.42");     //domino -> index 1
        VALID_IPS.add("129.21.37.69");     //argus -> index 2
        VALID_IPS.add("129.21.37.61");     //cyclops -> index 3
        VALID_IPS.add("129.21.37.70");     //midas -> index 4
        VALID_IPS.add("129.21.37.64");     //tiresias -> index 5
        VALID_IPS.add("129.21.37.71");     //carya -> index 6
        VALID_IPS.add("129.21.37.67");     //argonaut -> index 7

        TOTAL_SERVERS = VALID_IPS.size();
        IP_TO_INDEX = new HashMap<>();
        for (int i = 0; i < TOTAL_SERVERS; i++) {
            IP_TO_INDEX.put(VALID_IPS.get(i), i);
//            System.out.println(""+VALID_IPS.get(i)+", "+ i);
        }
    }

    public static String get_slave() {
        String selected;
        while (true) {
            lastIndexUsed = (lastIndexUsed + 1) % slave_sockets.size();
            if (!slave_sockets.get(lastIndexUsed).isClosed()) {
                selected = slave_sockets.get(lastIndexUsed).getInetAddress().getHostAddress() + ":8080";
                break;
            }
        }
        System.out.println("Selecting : " + selected);
        return selected;
//        int min_client = -1;
//        String min_key = "";
//        for(String key:Slave_Health.keySet()){
//            if(min_client <= Slave_Health.get(key)){
//                min_key = key;
//            }
//        }
//        System.out.println(Slave_Health.toString());
//        System.out.println("Slave selected is " + min_key);
//        return min_key.split(":")[0] + ":" + 8080;
    }

    public static void main(String[] args) throws Exception {
        Monitor_socket = new ServerSocket(7777);
        Monitor monitor = new Monitor();
        System.out.println("Monitor started! Listening at 7777");

        String sshcomm = "ssh amd5300@yes.cs.rit.edu";
        monitor.open_slave_comm();

        SimpleHttpServer httpServer = new SimpleHttpServer();
        httpServer.Start(8080);

    }

    private void open_slave_comm() {
        Slave_Listener slave_listener = new Slave_Listener();
        new Thread(slave_listener).start();
    }


    private class Client implements Runnable {

        public Client() {

        }

        @Override
        public void run() {

        }
    }


    class Slave implements Runnable {
        final DataOutputStream out;
        final DataInputStream in;
        final String IP;
        final int Port;
        final int id;

        public Slave(DataOutputStream out, DataInputStream in, String IP, int Port, int id) {
            this.out = out;
            this.in = in;
            this.IP = IP;
            this.Port = Port;
            this.id = id;
        }

        public void receive_heartbeat() {
            try {
                int load = Integer.parseInt(in.readUTF());
                System.out.println("Received load info: " + load + " --- from " + IP + " " + Port);
                Slave_Health.put(IP + " " + Port, load);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println("Running thread for slave " + IP + ":" + Port);
            while (true) {
                try {
                    Thread.sleep(3000);
                    receive_heartbeat();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Slave_Listener implements Runnable {

        @Override
        public void run() {
            while (true) {
                synchronized (slave_sockets) {
                    try {
                        slave_id++;
                        System.out.println("Waiting for slave at " + InetAddress.getLocalHost().getHostAddress() + ":7777 ..");
//                        System.out.println(Monitor_socket.toString());
                        slave_sockets.add(slave_id, Monitor_socket.accept());

//                        DataOutputStream out = new DataOutputStream(slave_sockets.get(slave_id).getOutputStream());
//                        DataInputStream in = new DataInputStream(slave_sockets.get(slave_id).getInputStream());
                        String IP = slave_sockets.get(slave_id).getInetAddress().getHostAddress();

                        int Port = slave_sockets.get(slave_id).getPort();
                        Slave_Health.put(IP + ":" + Port, 0);

                        System.out.println("Connected to " + IP + ":" + Port);

//                        slave_threads.add(new Thread(new Slave(out, in, IP, Port, slave_id)));
//                        slave_threads.get(slave_id).start();

//                        System.out.println(Slave_Health.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

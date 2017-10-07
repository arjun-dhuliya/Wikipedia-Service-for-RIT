import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Arjun Dhuliya
 *         Config Class contains helper function and static Variables that hold ip of valid servers
 */
public class Config {
    static final int TOTAL_SERVERS;
    static final ArrayList<String> VALID_IPS;
    static final int PORT_NUMBER = 6543;
    static final HashMap<String, Integer> IP_TO_INDEX;
    static final HashMap<Integer, Integer[]> neighbors;

    /*
     static constructor
     */
    static {
        VALID_IPS = new ArrayList<>();
        // Listed docker machines --> domino argus cyclops midas tiresias carya yes argonaut
        VALID_IPS.add("129.21.37.28");     //yes -> index 0
        VALID_IPS.add("129.21.37.42");     //domino -> index 1
        VALID_IPS.add("129.21.37.69");     //argus -> index 2
        VALID_IPS.add("129.21.37.61");     //cyclops -> index 3
        VALID_IPS.add("129.21.37.70");     //midas -> index 4
        VALID_IPS.add("129.21.37.64");     //tiresias -> index 5
        VALID_IPS.add("129.21.37.71");     //carya -> index 6
//        VALID_IPS.add("129.21.37.67");     //argonaut -> index 7
        TOTAL_SERVERS = VALID_IPS.size();
        IP_TO_INDEX = new HashMap<>();
        for (int i = 0; i < TOTAL_SERVERS; i++) {
            IP_TO_INDEX.put(VALID_IPS.get(i), i);
//            System.out.println(""+VALID_IPS.get(i)+", "+ i);
        }
        neighbors = new HashMap<>(7);
        neighbors.put(0, new Integer[]{1, 2});
        neighbors.put(1, new Integer[]{0, 3, 4});
        neighbors.put(2, new Integer[]{0, 5, 6});
        neighbors.put(3, new Integer[]{1});
        neighbors.put(4, new Integer[]{1});
        neighbors.put(5, new Integer[]{2});
        neighbors.put(6, new Integer[]{2});
    }

    /***
     * Helper Function to read bytes provided with a socket reference to receiver
     * @param socket, reference to socket
     * @return bytes
     * @throws IOException Exception
     */
    static byte[] readBytes(Socket socket) throws IOException {
        if (socket == null || socket.isClosed())
            return null;
        InputStream in = socket.getInputStream();
        DataInputStream dis = new DataInputStream(in);
        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }

    /***
     * Helper Function to send bytes provided with a socket reference to receiver
     * @param myByteArray, bytes to send
     * @param connection, socket reference
     * @throws IOException, Exception thrown
     */
    static void sendBytes(byte[] myByteArray, Socket connection) throws IOException {
        sendBytes(myByteArray, connection, myByteArray.length);
    }

    /***
     * Helper Function to send bytes provided with a socket reference to receiver
     * @param myByteArray, bytes
     * @param socket, socket
     * @param len, length
     * @throws IOException, Exception
     */
    private static void sendBytes(byte[] myByteArray, Socket socket, int len) throws IOException {
        if (socket == null || socket.isClosed())
            return;
        OutputStream out = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(len);
        if (len > 0) {

            dos.write(myByteArray, 0, len);
            dos.flush();
        }
//        System.out.println("Send data, to"+ Arrays.toString(myByteArray)+", "
//                +socket.getInetAddress().getHostName());
    }
}

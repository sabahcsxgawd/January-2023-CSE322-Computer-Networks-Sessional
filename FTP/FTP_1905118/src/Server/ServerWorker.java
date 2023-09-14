package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerWorker extends Thread {
    private final Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public ServerWorker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream((socket.getInputStream()));

            while(true) {
                String msg = (String) objectInputStream.readUnshared();
                if(msg.equals("SYN_C")) {
                    System.out.println("Got SYN from client\n");
                    objectOutputStream.writeUnshared((String)"ACK_S");
                }
                else {
                    System.out.println(msg);
                }
            }
        } catch (Exception e) {
        }
    }
}

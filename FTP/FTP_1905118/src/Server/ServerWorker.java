package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerWorker extends Thread {
    private final Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ServerWorker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream((socket.getInputStream()));

            while(true) {
                String msg = dataInputStream.readUTF();
                if(msg.equals("SYN_C")) {
                    System.out.println("Got SYN from client\n");
                    dataOutputStream.writeUTF("ACK_S");
                }
                else {
                    System.out.println(msg);
                }
            }
        } catch (Exception e) {
            try {
                this.dataInputStream.close();
                this.dataOutputStream.close();
                this.socket.close();
            } catch (Exception ee) {
//                ee.printStackTrace();
            }
//            e.printStackTrace();
        }
    }
}

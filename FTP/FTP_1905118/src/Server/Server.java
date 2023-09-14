package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6600)) {
            System.out.println("listening to port:6600");
            Socket clientSocket = serverSocket.accept();
            System.out.println(clientSocket + " connected\n");

            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataInputStream = new DataInputStream(clientSocket.getInputStream());

            String message;
            while (true) {
                try {
                    message = dataInputStream.readUTF();
                    System.out.println(message);
                    if (message.equalsIgnoreCase("exit()")) break;
                }
                catch (Exception e) {
                    clientSocket.close();
                    dataInputStream.close();
                    dataOutputStream.close();
                    e.printStackTrace();
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

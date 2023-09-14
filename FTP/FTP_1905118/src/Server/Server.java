package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        while(true) {
            try(ServerSocket serverSocket = new ServerSocket(6666)) {
                System.out.println("Waiting for Clients\n");
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected\n");

                ServerWorker serverWorker = new ServerWorker(clientSocket);
                serverWorker.start();

            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
}

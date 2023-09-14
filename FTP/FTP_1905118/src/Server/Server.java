package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static ConcurrentHashMap<String, String> clientStatus;
    public static void main(String[] args) {
        clientStatus = new ConcurrentHashMap<>();
        String clientDirsPath = "./src/Server/ClientDirs/";
        File clientDirFile = new File(clientDirsPath);

        for(File child : Objects.requireNonNull(clientDirFile.listFiles())) {
            String[] clientNamePath = child.getPath().split("[\\|/]");
            String clientName = clientNamePath[clientNamePath.length - 1];
            System.out.println(clientName);
            clientStatus.put(clientName, "Offline");
        }

        while(true) {
            try(ServerSocket serverSocket = new ServerSocket(6666)) {
                System.out.println("Waiting for Clients");
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected");

                ServerWorker serverWorker = new ServerWorker(clientSocket, clientStatus);
                serverWorker.start();

            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
}

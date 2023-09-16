package Server;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public static void main(String[] args) {
        ConcurrentHashMap<String, String> clientStatus = new ConcurrentHashMap<>();

        //assuming server always up so no extra data storage for unread msg is required
        ConcurrentHashMap<String, ArrayList<String>> unreadMessages = new ConcurrentHashMap<>();
        String clientDirsPath = "./src/Server/ClientDirs/";
        File clientDirFile = new File(clientDirsPath);

        for (File child : Objects.requireNonNull(clientDirFile.listFiles())) {
            String[] clientNamePath = child.getPath().split("\\\\|/"); // to remove separators from file path
            String clientName = clientNamePath[clientNamePath.length - 1]; // getting the actual file name
            System.out.println(clientName); // already added clients names
            clientStatus.put(clientName, "Offline");
            unreadMessages.put(clientName, new ArrayList<>());
        }

        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(6666)) {
                System.out.println("Waiting for Clients");
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket + " connected");

                ServerWorker serverWorker = new ServerWorker(clientSocket, clientStatus, unreadMessages);
                serverWorker.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

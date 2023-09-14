package Server;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorker extends Thread {
    private static ConcurrentHashMap<String, String> clientStatus;
    private static String clientDirsPath = "./src/Server/ClientDirs/";
    private static String clientDownloadsPath = "./src/Client/Downloads/";
    private final Socket socket;

    public ServerWorker(Socket socket, ConcurrentHashMap<String, String> clientStatusFromServer) {
        this.socket = socket;
        clientStatus = clientStatusFromServer;
    }

    public void run() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream((socket.getInputStream()));

            String clientUserName = (String) objectInputStream.readUnshared();
            clientUserName = clientUserName.trim();
            String clientConnStatus = clientStatus.get(clientUserName);
            String connMsgFromSever = "";
            if (clientConnStatus == null || clientConnStatus.equalsIgnoreCase("Offline")) {
                if (clientConnStatus == null) {
                    new File(clientDirsPath + clientUserName + "/public/").mkdirs();
                    new File(clientDirsPath + clientUserName + "/private/").mkdirs();
                    new File(clientDownloadsPath + clientUserName).mkdirs();
                }
                clientConnStatus  = "Online";
                clientStatus.put(clientUserName, clientConnStatus);
                connMsgFromSever = "Welcome to FTP";
                objectOutputStream.writeUnshared("Welcome to FTP");
            } else {
                objectOutputStream.writeUnshared("Client Already Connected");
                this.socket.close();
                objectInputStream.close();
                objectOutputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

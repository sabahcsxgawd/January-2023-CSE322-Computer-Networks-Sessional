package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6600)) {

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            File dir = new File("./src/Server");
            File[] dirListing = dir.listFiles();
            for(File child : dirListing) {
                System.out.println(child.getPath().replaceFirst("./src/Server/", ""));
            }

            String clientDirPath = "./src/Server/ClientDirs/";
            File publicFiles = new File(clientDirPath + "clientgg/public");
            publicFiles.mkdirs();
            File privateFiles = new File(clientDirPath + "clientgg/private");
            privateFiles.mkdirs();

//            while (true) {
//                System.out.print("input> ");
//                String message = scanner.nextLine();
//                dataOutputStream.writeUTF(message);
//                if (message.equalsIgnoreCase("exit()")) break;
//            }

            socket.close();
            dataInputStream.close();
            dataOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

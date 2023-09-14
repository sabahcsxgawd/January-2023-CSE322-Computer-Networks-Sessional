package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6666)) {

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

//            File dir = new File("./src/Server");
//            File[] dirListing = dir.listFiles();
//            for(File child : dirListing) {
//                System.out.println(child.getPath().replaceFirst("./src/Server/", ""));
//            }
//
//            String clientDirPath = "./src/Server/ClientDirs/";
//            File publicFiles = new File(clientDirPath + "clientgg/public");
//            publicFiles.mkdirs();
//            File privateFiles = new File(clientDirPath + "clientgg/private");
//            privateFiles.mkdirs();

            while (true) {
                String msg = scanner.nextLine();
                dataOutputStream.writeUTF(msg);
                if(dataInputStream.readUTF().equals("ACK_S")) {
                    System.out.println("Acked\n");
                    break;
                }
            }

            socket.close();
            dataInputStream.close();
            dataOutputStream.close();

        } catch (Exception e) {

        }
    }
}

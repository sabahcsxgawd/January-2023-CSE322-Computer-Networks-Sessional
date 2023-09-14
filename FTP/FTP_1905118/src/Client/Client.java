package Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6666)) {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            System.out.print("Enter your username to connect : ");
            String userName = scanner.nextLine();

            objectOutputStream.writeUnshared(userName);

            String connMsgFromServer = (String) objectInputStream.readUnshared();
            System.out.println(connMsgFromServer);

            if (connMsgFromServer.equalsIgnoreCase("Client Already Connected")) {
                socket.close();
                objectInputStream.close();
                objectOutputStream.close();
            } else {
                while (true) ;
            }
//            socket.close();
//            objectInputStream.close();
//            objectOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

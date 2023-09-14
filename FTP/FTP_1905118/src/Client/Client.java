package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6666)) {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            while (true) {
                String msg = scanner.nextLine();
                System.out.println(msg);
                objectOutputStream.writeUnshared(msg);
                String in = (String) objectInputStream.readUnshared();
                if(in.equals("ACK_S")) {
                    System.out.println("ACKED");
                    break;
                }
            }

            socket.close();
            objectInputStream.close();
            objectOutputStream.close();

        } catch (Exception e) {

        }
    }
}

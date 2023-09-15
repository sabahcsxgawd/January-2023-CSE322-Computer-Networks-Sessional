package Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6666)) {

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            System.out.print("Enter your username to connect : ");
            String userName = scanner.nextLine();

            oos.writeUnshared(userName);

            String connMsgFromServer = (String) ois.readUnshared();
            System.out.println(connMsgFromServer);

            if (connMsgFromServer.equalsIgnoreCase("Client Already Connected")) {
                socket.close();
                ois.close();
                oos.close();
            } else {
                while (true) {
                    System.out.println((String) ois.readUnshared());
                    String choice = scanner.nextLine();
                    oos.writeUnshared(choice);
                    int response = (int) ois.readUnshared();
                    if(response == -1) {
                        System.out.println(ois.readUnshared());
                    }
                    else if(response == 0) {
                        System.out.println(ois.readUnshared());
                        socket.close();
                        ois.close();
                        oos.close();
                        break;
                    }
                    else if(response == 1) {
                        System.out.println(ois.readUnshared());
                    }
                    else if(response == 2) {
                        System.out.println(ois.readUnshared());
                    }
                    else if(response == 3) {
                        System.out.println(ois.readUnshared());
                    }
                    else if(response == 4) {
                        System.out.println(ois.readUnshared());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

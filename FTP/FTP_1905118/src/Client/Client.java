package Client;

import FileRequest.FileRequest;

import java.io.FileOutputStream;
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
                int response;
                while (true) {
                    String serverMSG = (String) ois.readUnshared();
                    System.out.println(serverMSG);
                    String choice = scanner.nextLine();
                    oos.writeUnshared(choice);
                    response = (int) ois.readUnshared();
                    L: for(int i = 0; i <= 1; i++) {
                        if (response == -1) {
                            System.out.println(ois.readUnshared());
                        }
                        else if (response == 0) {
                            System.out.println(ois.readUnshared());
                            socket.close();
                            ois.close();
                            oos.close();
                            break;
                        }
                        else if (response == 1) {
                            System.out.println(ois.readUnshared());
                        }
                        else if (response == 2) {
                            System.out.println(ois.readUnshared());
                        }
                        else if (response == 3) {
                            System.out.println(ois.readUnshared());
                        }
                        else if (response == 4) {
                            System.out.println(ois.readUnshared());
                        }
                        else if (response == 5) {
                            System.out.println(ois.readUnshared());
                            String myAccessTypeChoice = scanner.nextLine();
                            oos.writeUnshared(myAccessTypeChoice);
                            String serverMsg1 = (String) ois.readUnshared();
                            System.out.println(serverMsg1);
                            if (serverMsg1.contains("Bad Choice")) {
                                continue;
                            } else {
                                System.out.println((String) ois.readUnshared());
                                String fileChoice = scanner.nextLine();
                                oos.writeUnshared(fileChoice);
                                String serverMsg2 = (String) ois.readUnshared();
                                if (serverMsg2.contains("Bad Choice")) {
                                    System.out.println(serverMsg2);
                                    continue;
                                } else {
                                    System.out.println("Downloading File " + serverMsg2);

                                    // actual file receiving gets started
                                    int receivedBytes = 0;
                                    FileOutputStream fos = new FileOutputStream("./src/Client/Downloads/" + userName + "/" + serverMsg2);
                                    byte[] buffer = new byte[8192];
                                    while ((receivedBytes = ois.read(buffer)) != -1) {
                                        fos.write(buffer, 0, receivedBytes);
                                    }
                                    fos.close();
                                    System.out.println((String) ois.readUnshared());
                                }
                            }
                        }
                        else if (response == 6) {
                            System.out.println((String) ois.readUnshared());
                            String clientFileChoice = scanner.nextLine();
                            oos.writeUnshared(clientFileChoice);
                            String serverMsg1 = (String) ois.readUnshared();
                            if (serverMsg1.contains("Bad Choice")) {
                                System.out.println(serverMsg1);
                                continue;
                            } else {
                                System.out.println("Downloading file " + serverMsg1);

                                // actual file receiving gets started
                                int receivedBytes = 0;
                                FileOutputStream fos = new FileOutputStream("./src/Client/Downloads/" + userName + "/" + serverMsg1);
                                byte[] buffer = new byte[8192];
                                while ((receivedBytes = ois.read(buffer)) != -1) {
                                    fos.write(buffer, 0, receivedBytes);
                                }
                                fos.close();
                                System.out.println((String) ois.readUnshared());
                            }
                        }
                        else if (response == 7) {
                            System.out.println("Please provide a short description for the requested file :");
                            String requestedFileDescription = scanner.nextLine();
                            oos.writeUnshared(new FileRequest(requestedFileDescription, userName));
                        }
                        else if (response == 777) {
                            System.out.println((String) ois.readUnshared());
                            response = (int) ois.readUnshared();
                            continue L;
                        }
                        break L;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

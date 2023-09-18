package Client;

import FileRequest.FileRequest;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
                new File("./src/Client/Downloads/" + userName + '/').mkdirs();
                while (true) {
                    String serverMSG = (String) ois.readUnshared();
                    if(serverMSG.equalsIgnoreCase("UPLOAD_ACK")) {
                        continue;
                    }
                    System.out.println(serverMSG);
                    String choice = scanner.nextLine();
                    oos.writeUnshared(choice);
                    response = (int) ois.readUnshared();
                    L:
                    for (int i = 0; i <= 1; i++) {
                        if (response == -1) {
                            System.out.println(ois.readUnshared());
                        }
                        else if (response == 0) {
                            System.out.println(ois.readUnshared());
                            ois.close();
                            oos.close();
                            socket.close();
                            return;
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
                                break L;
                            } else {
                                String serverMSG2 = (String) ois.readUnshared();
                                if (serverMSG2.isEmpty()) {
                                    break L;
                                }
                                System.out.println(serverMSG2);
                                String fileChoice = scanner.nextLine();
                                oos.writeUnshared(fileChoice);
                                String serverMsg2 = (String) ois.readUnshared();
                                if (serverMsg2.contains("Bad Choice")) {
                                    System.out.println(serverMsg2);
                                    break L;
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
                            String serverMSG1 = (String) ois.readUnshared();
                            System.out.println(serverMSG1);
                            if (!serverMSG1.equalsIgnoreCase("Other Clients have no Public Files")) {
                                String clientFileChoice = scanner.nextLine();
                                oos.writeUnshared(clientFileChoice);
                                String serverMsg1 = (String) ois.readUnshared();
                                if (serverMsg1.contains("Bad Choice")) {
                                    System.out.println(serverMsg1);
                                    break L;
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
                        }
                        else if (response == 7) {
                            System.out.println("Please provide a short description for the requested file :");
                            String requestedFileDescription = scanner.nextLine();
                            oos.writeUnshared(new FileRequest(requestedFileDescription, userName));
                        }
                        else if (response == 8) {
                            System.out.println((String) ois.readUnshared());
                            String s_uploadChoice = scanner.nextLine();
                            oos.writeUnshared(s_uploadChoice);
                            String serverResponse1 = (String) ois.readUnshared();
                            if (serverResponse1.equalsIgnoreCase("0")) {
                                System.out.println("\n");
                            } else if (serverResponse1.equalsIgnoreCase("1")) {
                                System.out.println("\n");
                            } else if (serverResponse1.equalsIgnoreCase("2")) {
                                String reqstList = (String) ois.readUnshared();
                                if (reqstList.equalsIgnoreCase("No requests to fulfill")) {
                                    System.out.println(reqstList);
                                    break L;
                                } else {
                                    System.out.println(reqstList);
                                    String reqstListChoice = scanner.nextLine();
                                    oos.writeUnshared(reqstListChoice);
                                    String serverResponse2 = (String) ois.readUnshared();
                                    if (serverResponse2.equalsIgnoreCase("Bad Choice")) {
                                        System.out.println(serverResponse2);
                                        break L;
                                    } else {
                                        System.out.println("\n");
                                    }
                                }
                            } else {
                                System.out.println(serverResponse1);
                                break L;
                            }

                            // need to pick file
                            File[] clientUploadableFiles = new File("./src/Client/Downloads/" + userName).listFiles();
                            if (clientUploadableFiles == null || clientUploadableFiles.length == 0) {
                                System.out.println("No uploadable files");
                                oos.writeUnshared("No uploadable files");
                                break L;
                            } else {
                                oos.writeUnshared("Something to upload");
                                int whichFile = 0;
                                for (File child : clientUploadableFiles) {
//                                    String[] fileNameArr = child.getPath().split("\\\\|/");
//                                    String fileName = fileNameArr[fileNameArr.length - 1];
                                    System.out.println("Type " + whichFile++ + " to upload " + child.getName());
                                }
                                whichFile = -1;
                                String s_whichFile = scanner.nextLine();
                                whichFile = Integer.parseInt(s_whichFile);
                                if (0 <= whichFile && whichFile < clientUploadableFiles.length) {
                                    // send file name and size to server
                                    String fileName = clientUploadableFiles[whichFile].getName();
                                    long fileSize = clientUploadableFiles[whichFile].length();
                                    oos.writeUnshared(fileName);
                                    oos.writeUnshared(fileSize);
                                    String serverMSG3 = (String) ois.readUnshared();
                                    if (serverMSG3.equalsIgnoreCase("Buffer_Overflow")) {
                                        System.out.println(serverMSG3);
                                        break L;
                                    } else {
                                        String fileID = (String) ois.readUnshared();
                                        int chunkSize = (int) ois.readUnshared();
                                        String uploadStatMsg = "NOT_LAST_CHUNK";
                                        int sentBytes = 0;
                                        byte[] buffer = new byte[chunkSize];
                                        FileInputStream fis = new FileInputStream(clientUploadableFiles[whichFile]);
                                        while ((sentBytes = fis.read(buffer, 0, chunkSize)) != -1) {
                                            fileSize -= sentBytes;
                                            if (fileSize <= 0) {
                                                uploadStatMsg = "LAST_CHUNK";
                                            }
                                            // send data and status
                                            oos.writeUnshared(uploadStatMsg);
                                            oos.writeUnshared(sentBytes);
                                            oos.write(buffer, 0, sentBytes);
                                            oos.flush();

                                            socket.setSoTimeout(30000);
                                            try {
                                                String serverMSG4 = (String) ois.readUnshared();
                                                if (serverMSG4.equalsIgnoreCase("UPLOAD_ACK")) {
                                                    if (uploadStatMsg.equalsIgnoreCase("LAST_CHUNK")) {
                                                        System.out.println((String) ois.readUnshared());
                                                        break;
                                                    }
                                                }
                                            } catch (SocketTimeoutException e) {
                                                uploadStatMsg = "UPLOAD_TIMEOUT";
                                                oos.writeUnshared(uploadStatMsg);
                                                System.out.println("Upload failed due to timeout");
                                                break;
                                            }
                                        }
                                        fis.close();
                                    }
                                } else {
                                    System.out.println("Bad Choice");
                                    break L;
                                }
                            }
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

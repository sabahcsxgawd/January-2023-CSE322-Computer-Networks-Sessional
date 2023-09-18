package Server;

import FileRequest.FileRequest;

import javax.swing.plaf.metal.MetalBorders;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerWorker extends Thread {

    private static final int MIN_CHUNK_SIZE = 4096; // 4KB
    private static final int MAX_CHUNK_SIZE = 8192; // 8KB
    private static final int MAX_BUFFER_SIZE = 67108864; // 64MB

    private static ConcurrentHashMap<String, String> clientStatus;
    private static ConcurrentHashMap<String, ArrayList<String>> unreadMessages;

    private static ConcurrentHashMap<String, ArrayList<Chunk>> chunkConcurrentHashMap;

    private static ConcurrentHashMap<String, ObjectOutputStream> allClientOOS;
    private static ArrayList<FileRequest> fileRequestArrayList;

    private static final String clientDirsPath = "./src/Server/ClientDirs/";
    private static final String clientDownloadsPath = "./src/Client/Downloads/";
    private final Socket socket;

    private static final String[] optionsMenu = {
            "Log Out",
            "View All Connected Clients with Status(at least once)",
            "View own private and public files",
            "View others public files",
            "View all unread messages",
            "Download own file",
            "Download others public file",
            "Make a file request",
            "Upload a file"
    };

    public ServerWorker(Socket socket) {
        this.socket = socket;
    }

    public static void setClientStatus(ConcurrentHashMap<String, String> clientStatus) {
        ServerWorker.clientStatus = clientStatus;
    }

    public static void setUnreadMessages(ConcurrentHashMap<String, ArrayList<String>> unreadMessages) {
        ServerWorker.unreadMessages = unreadMessages;
    }

    public static void setFileRequestArrayList(ArrayList<FileRequest> fileRequestArrayList) {
        ServerWorker.fileRequestArrayList = fileRequestArrayList;
    }

    public static void setAllClientOOS(ConcurrentHashMap<String, ObjectOutputStream> allClientOOS) {
        ServerWorker.allClientOOS = allClientOOS;
    }

    public static void setChunkConcurrentHashMap(ConcurrentHashMap<String, ArrayList<Chunk>> chunkConcurrentHashMap) {
        ServerWorker.chunkConcurrentHashMap = chunkConcurrentHashMap;
    }

    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream((socket.getInputStream()));

            String clientName = (String) ois.readUnshared();
            clientName = clientName.trim();
            String clientConnStatus = clientStatus.get(clientName);

            if (clientConnStatus == null || clientConnStatus.equalsIgnoreCase("Offline")) {
                if (clientConnStatus == null) {
                    new File(clientDirsPath + clientName + "/public/").mkdirs();
                    new File(clientDirsPath + clientName + "/private/").mkdirs();
                    new File(clientDownloadsPath + clientName + '/').mkdirs();
                }
                clientConnStatus = "Online";
                clientStatus.put(clientName, clientConnStatus);
                allClientOOS.put(clientName, oos);
                if (unreadMessages.get(clientName) == null) {
                    unreadMessages.put(clientName, new ArrayList<>());
                }
                oos.writeUnshared("Welcome to FTP");

                while (true) {
                    // send optionsMenu
                    sendOptionsMenu(oos);

                    // get optionsMenuChoice
                    int optionsMenuChoice = getOptionsMenuChoice(ois);
                    oos.writeUnshared(optionsMenuChoice);

                    // do choiceWiseWork
                    if (optionsMenuChoice == -1) {
                        oos.writeUnshared("Bad Choice. Please choose correctly");
                    } else if (optionsMenuChoice == 0) {
                        clientStatus.put(clientName, "Offline");
                        allClientOOS.remove(clientName);
                        this.logOut(ois, oos, this.socket);
                        break;
                    } else if (optionsMenuChoice == 1) {
                        this.sendAllClientStatus(oos);
                    } else if (optionsMenuChoice == 2) {
                        this.sendOwnFileInfo(oos, clientName);
                    } else if (optionsMenuChoice == 3) {
                        this.sendOtherPublicFileInfo(oos, clientName);
                    } else if (optionsMenuChoice == 4) {
                        String unreadMsg = "Here are all of your unread messages : \n";
                        for (String msg : unreadMessages.get(clientName)) {
                            unreadMsg += msg + "\n\n";
                        }
                        unreadMessages.get(clientName).clear();
                        oos.writeUnshared(unreadMsg);
                    } else if (optionsMenuChoice == 5) {
                        this.ownFileDownload(clientName, oos, ois);
                    } else if (optionsMenuChoice == 6) {
                        this.othersFileDownload(clientName, oos, ois);
                    }
                    else if(optionsMenuChoice == 7) {
                        this.broadcastFileRequest(ois);
                    }
                    else if(optionsMenuChoice == 8) {
                        this.handleClientFileUpload(clientName, oos, ois);
                    }
                }

            } else {
                oos.writeUnshared("Client Already Connected");
                this.socket.close();
                ois.close();
                oos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClientFileUpload(String clientName, ObjectOutputStream oos, ObjectInputStream ois) {
        String uploadChoice = "Type 0 to upload private file\n" +
                              "Type 1 to upload public file [not response to a request]\n" +
                              "Type 2 to upload public file [response to a request]\n";
        try {
            oos.writeUnshared(uploadChoice);
            int clientUploadChoice = Integer.parseInt(((String) ois.readUnshared()).trim());
            if(0 <= clientUploadChoice && clientUploadChoice < 3) {
                oos.writeUnshared(Integer.toString(clientUploadChoice));
                String accessType = "";
                int reqstListChoice = -1;
                List<FileRequest> fileRequestArrayList1 = new ArrayList<>();
                if(clientUploadChoice == 0) {
                    accessType = "/private/";
                }
                else if(clientUploadChoice == 1) {
                    accessType = "/public/";
                }
                else if(clientUploadChoice == 2) {
                    accessType = "/public/";
                    String reqstList = "";
                    fileRequestArrayList1 =
                            fileRequestArrayList.stream().
                                    filter(fr -> !(fr.getWhoRequested().equalsIgnoreCase(clientName))).
                                    collect(Collectors.toList());
                    if(fileRequestArrayList1.isEmpty()) {
                        reqstList = "No requests to fulfill";
                        oos.writeUnshared(reqstList);
                        return;
                    }
                    else {
                        for(int i = 0; i < fileRequestArrayList1.size(); i++) {
                            reqstList += "Type " + i + " to fulfill\n" +
                                    "Request ID : " + fileRequestArrayList1.get(i).getRequestID() + '\n' +
                                    "Requested File Description : " + fileRequestArrayList1.get(i).getFileDescription() + '\n';
                        }
                        oos.writeUnshared(reqstList);
                        reqstListChoice = Integer.parseInt(((String) ois.readUnshared()).trim());
                        if(0 <= reqstListChoice && reqstListChoice < fileRequestArrayList1.size()) {
                            oos.writeUnshared(Integer.toString(reqstListChoice));
                        }
                        else {
                            oos.writeUnshared("Bad Choice");
                            return;
                        }
                    }
                }

                if(((String) ois.readUnshared()).equalsIgnoreCase("No uploadable files")) {
                    return;
                }
                else {
                    String clientUploadFileName = (String) ois.readUnshared();
                    String clientUploadFileID = "";
                    long clientUploadFileSize = (long) ois.readUnshared();
                    int chunkSize = -1;
                    // need to check available buffer + filesize <= max buffer
                    if(this.getAvailableBufferSize() + clientUploadFileSize <= MAX_BUFFER_SIZE) {
                        oos.writeUnshared("Confirming");
                        clientUploadFileID = this.generateFileID();
                        chunkSize = this.generateRandomChunkSize();
                        chunkConcurrentHashMap.put(clientUploadFileID, new ArrayList<>());
                        oos.writeUnshared(clientUploadFileID);
                        oos.writeUnshared(chunkSize);
                        byte[] buffer = new byte[chunkSize];
                        int receivedBytes = 0;
                        while (true) {
                            String uploadStatMSG = (String) ois.readUnshared();
                            if(uploadStatMSG.equalsIgnoreCase("UPLOAD_TIMEOUT")) {
                                chunkConcurrentHashMap.remove(clientUploadFileID);
                                break;
                            }
                            receivedBytes = (int) ois.readUnshared();
                            ois.readFully(buffer, 0, receivedBytes);
                            chunkConcurrentHashMap.get(clientUploadFileID).add(new Chunk(buffer, receivedBytes));
                            // TODO need to send ACK
                            // TODO test timeout ; comment out the line to test for timeout
//                            Thread.sleep(35000);
                            oos.writeUnshared("UPLOAD_ACK");
                            if(uploadStatMSG.equalsIgnoreCase("LAST_CHUNK")) {
                                // check if file size matches or not
                                if(matchReceivedFileSize(clientUploadFileID, clientUploadFileSize)) {
                                    oos.writeUnshared("UPLOAD_SUCCESS");
                                    // TODO build file and send message to who reqstd
                                    FileOutputStream fos = new FileOutputStream(clientDirsPath + clientName + accessType + clientUploadFileName);
                                    for(Chunk chunk : chunkConcurrentHashMap.get(clientUploadFileID)) {
                                        fos.write(chunk.getData(), 0, chunk.getLen());
                                    }
                                    fos.close();
                                    if(reqstListChoice != -1) {
                                        String whoRequested = fileRequestArrayList1.get(reqstListChoice).getWhoRequested();
                                        unreadMessages.get(whoRequested).add(clientName + " has uploaded your requested file");
                                    }
                                }
                                else {
                                    oos.writeUnshared("UPLOAD_ERROR");
                                    new File(clientDirsPath + clientName + accessType + clientUploadFileName).delete();
                                }
                                chunkConcurrentHashMap.get(clientUploadFileID).clear();
                                return;
                            }
                        }
                    }
                    else {
                        oos.writeUnshared("Buffer_Overflow");
                        return;
                    }
                }
            }
            else {
                oos.writeUnshared("Bad Choice");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void broadcastFileRequest(ObjectInputStream ois) {
        try {
            Object o = ois.readUnshared();
            if(o != null && o instanceof FileRequest) {
                fileRequestArrayList.add((FileRequest) o);
                String broadcastMsg = "\n-----New File Request-----\n" +
                        "File Request ID : " + ((FileRequest) o).getRequestID() + "\n" +
                        "File Requested by : " + ((FileRequest) o).getWhoRequested() + "\n" +
                        "File Short Description : " + ((FileRequest) o).getFileDescription() + "\n";

                // TODO
                for(String otherClient : Collections.list(allClientOOS.keys())) {
                    if(!otherClient.equalsIgnoreCase(((FileRequest) o).getWhoRequested())) {
                        System.out.println(otherClient);
                        ObjectOutputStream tempOOS = allClientOOS.get(otherClient);
                        tempOOS.writeUnshared((int)777);
                        tempOOS.writeUnshared(broadcastMsg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendOptionsMenu(ObjectOutputStream oos) {
        String optionsMenuStr = "";
        for (int i = 0; i < optionsMenu.length; i++) {
            optionsMenuStr += "Type " + i + " to " + optionsMenu[i] + '\n';
        }
        try {
            oos.writeUnshared(optionsMenuStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getOptionsMenuChoice(ObjectInputStream ois) {
        try {
            String choice = (String) ois.readUnshared();
            int choice_i = Integer.parseInt(choice.trim());
            if (0 <= choice_i && choice_i < optionsMenu.length) {
                return choice_i;
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }

    }

    private void sendAllClientStatus(ObjectOutputStream oos) {
        String allClientStatus = "Client Name\t\t\tClient Active Status\n";
        for (Map.Entry<String, String> elem : clientStatus.entrySet()) {
            allClientStatus += elem.getKey() + "\t\t\t\t" + elem.getValue() + '\n';
        }
        try {
            oos.writeUnshared(allClientStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getFileInfo(String clientName, int accessType) {
        String access = "";
        ArrayList<String> files = new ArrayList<>();
        if (accessType == 0) {
            access = "/private/";
        } else if (accessType == 1) {
            access = "/public/";
        }
        String fileDir = clientDirsPath + clientName + access;
        File[] fileArr = new File(fileDir).listFiles();

        for (File child : Objects.requireNonNull(fileArr)) {
//            String[] filePathArr = child.getPath().split("\\\\|/");
//            String file = filePathArr[filePathArr.length - 1];
            files.add(child.getName());
        }
        return files;
    }

    private void sendOwnFileInfo(ObjectOutputStream oos, String clientName) {
        String ownFileInfo = "";

        // private
        ownFileInfo += "My private files : \n";
        for (String file : getFileInfo(clientName, 0)) {
            ownFileInfo += file + '\n';
        }

        ownFileInfo += "My public files : \n";
        for (String file : getFileInfo(clientName, 1)) {
            ownFileInfo += file + '\n';
        }

        try {
            oos.writeUnshared(ownFileInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendOtherPublicFileInfo(ObjectOutputStream oos, String clientName) {
        String othersFileInfo = "";
        for (String otherClient : Collections.list(clientStatus.keys())) {
            if (!otherClient.equalsIgnoreCase(clientName)) {
                othersFileInfo += "Public Files of Client " + otherClient + " :\n";
                ArrayList<String> othersFiles = getFileInfo(otherClient, 1);
                for (String otherFile : othersFiles) {
                    othersFileInfo += otherFile + '\n';
                }
            }
        }

        try {
            oos.writeUnshared(othersFileInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ownFileDownload(String clientName, ObjectOutputStream oos, ObjectInputStream ois) {
        String accessTypeChoice =
                "Type 0 to download own private file\n" + "Type 1 to download own public file";
        try {
            oos.writeUnshared(accessTypeChoice);
            String clientChoice = (String) ois.readUnshared();
            clientChoice = clientChoice.trim();
            ArrayList<String> displayFiles = new ArrayList<>();
            if (clientChoice.equalsIgnoreCase("0")) {
                displayFiles = this.getFileInfo(clientName, 0);
                clientChoice = "/private/";
                oos.writeUnshared("Here are your private files for download :");
            } else if (clientChoice.equalsIgnoreCase("1")) {
                clientChoice = "/public/";
                displayFiles = this.getFileInfo(clientName, 1);
                oos.writeUnshared("Here are your public files for download :");
            } else {
                oos.writeUnshared("Bad Choice. Please choose correctly");
            }
            if (!displayFiles.isEmpty()) {
                String downloadableFilesInfo = "";
                for (int i = 0; i < displayFiles.size(); i++) {
                    downloadableFilesInfo += "Type " + i + " to download file " + displayFiles.get(i) + '\n';
                }
                oos.writeUnshared(downloadableFilesInfo);
                String clientFileChoice = (String) ois.readUnshared();
                clientFileChoice = clientFileChoice.trim();
                int iFileChoice = Integer.parseInt(clientFileChoice);
                if (0 <= iFileChoice && iFileChoice < displayFiles.size()) {
                    oos.writeUnshared(displayFiles.get(iFileChoice));

                    // actual file sending gets started
                    FileInputStream fis = new FileInputStream(new File(clientDirsPath + clientName + clientChoice + displayFiles.get(iFileChoice)));
                    int sentBytes = 0;
                    byte[] buffer = new byte[MAX_CHUNK_SIZE];
                    while ((sentBytes = fis.read(buffer)) != -1) {
                        oos.write(buffer, 0, sentBytes);
                        oos.flush();
                    }
                    fis.close();
                    oos.writeUnshared("Download Successful");

                } else {
                    oos.writeUnshared("Bad Choice");
                }
            }
            else {
                oos.writeUnshared("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void othersFileDownload(String clientName, ObjectOutputStream oos, ObjectInputStream ois) {
        ArrayList<Pair> pairArrayList = new ArrayList<>();
        String downloadChoice = "";
        int index = 0;

        for (String otherClient : Collections.list(clientStatus.keys())) {
            if (!otherClient.equalsIgnoreCase(clientName)) {
                ArrayList<String> otherClientFiles = this.getFileInfo(otherClient, 1);
                for (String otherFile : otherClientFiles) {
                    downloadChoice += "Type " + index++ + " to download file " + otherFile + " [Owner : " + otherClient + "]\n";
                    pairArrayList.add(new Pair(otherClient, otherFile));
                }
            }
        }

        try {
            if(downloadChoice.equalsIgnoreCase("")) {
                downloadChoice = "Other Clients have no Public Files";
                oos.writeUnshared(downloadChoice);
                return;
            }
            oos.writeUnshared(downloadChoice);
            String fileChoice = (String) ois.readUnshared();
            fileChoice = fileChoice.trim();
            int iFileChoice = Integer.parseInt(fileChoice);
            if (0 <= iFileChoice && iFileChoice < index) {
                oos.writeUnshared(pairArrayList.get(iFileChoice).getFileName());

                // actual file sending gets started
                FileInputStream fis = new FileInputStream(clientDirsPath + pairArrayList.get(iFileChoice).getOwnerName() + "/public/" + pairArrayList.get(iFileChoice).getFileName());
                int sentBytes = 0;
                byte[] buffer = new byte[MAX_CHUNK_SIZE];
                while ((sentBytes = fis.read(buffer)) != -1) {
                    oos.write(buffer, 0, sentBytes);
                    oos.flush();
                }
                fis.close();
                oos.writeUnshared("Download Successful");
            } else {
                oos.writeUnshared("Bad Choice. Please choose correctly");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void logOut(ObjectInputStream ois, ObjectOutputStream oos, Socket socket) {
        try {
            oos.writeUnshared("GoodBye, See you again");
            socket.close();
            ois.close();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getAvailableBufferSize() {
        long availableBufferSize = 0;
        for(String fileID : Collections.list(chunkConcurrentHashMap.keys())) {
            for(Chunk chunk : chunkConcurrentHashMap.get(fileID)) {
                availableBufferSize += chunk.getLen();
            }
        }
        return availableBufferSize;
    }
    private int generateRandomChunkSize() {
        return (new Random()).nextInt(MAX_CHUNK_SIZE - MIN_CHUNK_SIZE + 1) + MIN_CHUNK_SIZE;
    }
    private String generateFileID() {
        return UUID.randomUUID().toString();
    }

    private boolean matchReceivedFileSize(String fileID, long fileSize) {
        long tempFileSize = 0;
        for(Chunk chunk : chunkConcurrentHashMap.get(fileID)) {
            tempFileSize += chunk.getLen();
        }
        return (tempFileSize == fileSize);
    }
}

class Pair {
    private String ownerName;

    public String getOwnerName() {
        return ownerName;
    }

    public String getFileName() {
        return fileName;
    }

    private String fileName;

    Pair(String ownerName, String fileName) {
        this.ownerName = ownerName;
        this.fileName = fileName;
    }
}
class Chunk {
    private byte[] data;
    private int len;

    public Chunk(byte[] data, int len) {
        this.data = data.clone();
        this.len = len;
    }

    public byte[] getData() {
        return data;
    }

    public int getLen() {
        return len;
    }
}
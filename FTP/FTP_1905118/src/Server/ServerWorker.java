package Server;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorker extends Thread {
    private static ConcurrentHashMap<String, String> clientStatus;
    private static String clientDirsPath = "./src/Server/ClientDirs/";
    private static String clientDownloadsPath = "./src/Client/Downloads/";
    private final Socket socket;

    private static final String[] optionsMenu = {
            "Log Out",
            "View All Connected Clients with Status(at least once)",
            "View own private and public files",
            "View others public files",
            "View all unread messages",
            // TODO file download, upload, request 
    };

    public ServerWorker(Socket socket, ConcurrentHashMap<String, String> clientStatusFromServer) {
        this.socket = socket;
        clientStatus = clientStatusFromServer;
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
                clientConnStatus  = "Online";
                clientStatus.put(clientName, clientConnStatus);
                oos.writeUnshared("Welcome to FTP");
                
                while (true) {
                    // send optionsMenu
                    sendOptionsMenu(oos);
                    // get optionsMenuChoice
                    int optionsMenuChoice = getOptionsMenuChoice(ois);
                    // do choiceWiseWork
                    if(optionsMenuChoice == -1) {
                        oos.writeUnshared("Bad Choice. Please choose correctly");
                    }
                    else if(optionsMenuChoice == 0) {
                        this.logOut(ois, oos, this.socket);
                    }
                    else if(optionsMenuChoice == 1) {
                        this.sendAllClientStatus(oos);
                    }
                    else if(optionsMenuChoice == 2) {
                        this.sendOwnFileInfo(oos, clientName);
                    }
                    else if(optionsMenuChoice == 3) {
                        this.sendOtherPublicFileInfo(oos, clientName);
                    }
                    else if(optionsMenuChoice == 4) {
                        // TODO view unread msgs
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

    private void sendOptionsMenu(ObjectOutputStream oos) {
        String optionsMenuStr = "";
        for(int i = 0; i < optionsMenu.length; i++) {
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
            int choice_i = Integer.parseInt(choice);
            if(0 <= choice_i && choice_i < optionsMenu.length) {
                return choice_i;
            }
            else {
                return -1;
            }
        }
        catch (Exception e) {
            return -1;
        }
        
    }
    
    private void sendAllClientStatus(ObjectOutputStream oos) {
        try {
            oos.writeUnshared(clientStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getFileInfo(String clientName, int accessType) {
        String access = "";
        ArrayList<String> files = new ArrayList<>();
        if(accessType == 0) {
            access = "/private/";
        }
        else if(accessType == 1) {
            access = "/public/";
        }
        String fileDir = clientDirsPath + clientName + access;
        File[] fileArr = new File(fileDir).listFiles();

        for(File child : Objects.requireNonNull(fileArr)) {
            String[] filePathArr = child.getPath().split("[\\|/]");
            String file = filePathArr[fileArr.length - 1];
            files.add(file);
        }
        return files;
    }

    private void sendOwnFileInfo(ObjectOutputStream oos, String clientName) {
        String ownFileInfo = "";

        // private
        ownFileInfo += "My private files : \n";
        for(String file : getFileInfo(clientName, 0)) {
            ownFileInfo += file + '\n';
        }

        ownFileInfo += "My public files : \n";
        for(String file : getFileInfo(clientName, 1)) {
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
        for(String otherClient : Collections.list(clientStatus.keys())) {
            if(!otherClient.equalsIgnoreCase(clientName)) {
                othersFileInfo += "Public Files of Client " + otherClient + " :\n";
                ArrayList<String> othersFiles = getFileInfo(otherClient, 1);
                for(String otherFile : othersFiles) {
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

    private void logOut(ObjectInputStream ois, ObjectOutputStream oos, Socket socket) {
        try {
            socket.close();
            ois.close();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}

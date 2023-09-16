package FileRequest;

import java.io.Serializable;
import java.util.UUID;

public class FileRequest implements Serializable {
    private String requestID;
    private String whoRequested;
    private String fileDescription;

    public String getRequestID() {
        return requestID;
    }

    public String getWhoRequested() {
        return whoRequested;
    }

    public String getFileDescription() {
        return fileDescription;
    }
    public FileRequest(String fileDescription, String whoRequested) {
        this.requestID = UUID.randomUUID().toString();
        this.fileDescription = fileDescription;
        this.whoRequested = whoRequested;
    }
}

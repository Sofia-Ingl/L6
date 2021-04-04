package shared.serializable;

public class ServerResponse {

    String responseToPrint;

    public ServerResponse(String responseToPrint) {
        this.responseToPrint = responseToPrint;
    }

    public String getResponseToPrint() {
        return responseToPrint;
    }
}

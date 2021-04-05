package shared.serializable;

import shared.util.CommandExecutionCode;

import java.io.Serializable;

public class ServerResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String responseToPrint;
    private CommandExecutionCode code;

    public ServerResponse(CommandExecutionCode code, String responseToPrint) {
        this.code = code;
        this.responseToPrint = responseToPrint;
    }

    public String getResponseToPrint() {
        return responseToPrint;
    }

    public CommandExecutionCode getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "responseToPrint='" + responseToPrint + '\'' +
                ", code=" + code +
                '}';
    }
}

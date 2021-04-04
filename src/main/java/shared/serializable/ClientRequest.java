package shared.serializable;

public class ClientRequest {

    String command;
    String commandArgument;
    Object createdObject;

    public ClientRequest(String command, String commandArgument, Object createdObject) {
        this.command = command;
        this.commandArgument = commandArgument;
        this.createdObject = createdObject;
    }

    public Object getCreatedObject() {
        return createdObject;
    }

    public String getCommand() {
        return command;
    }

    public String getCommandArgument() {
        return commandArgument;
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "command='" + command + '\'' +
                ", commandArgument='" + commandArgument + '\'' +
                ", createdObject=" + createdObject +
                '}';
    }
}

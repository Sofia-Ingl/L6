package server.util;

import server.commands.Command;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;
import shared.serializable.ServerResponse;
import shared.util.CommandExecutionCode;

public class RequestProcessor {

    CommandWrapper commandWrapper;

    public RequestProcessor(CommandWrapper commandWrapper) {
        this.commandWrapper = commandWrapper;
    }

    public ServerResponse processRequest(ClientRequest request) {

        Command command = commandWrapper.getAllCommandsAvailable().get(request.getCommand());
        CommandExecutionCode code = CommandExecutionCode.SUCCESS;
        Pair<Boolean, String> commandResult = command.execute(request.getCommandArgument(), request.getCreatedObject());
//        if (command.getName().equals("exit")) {
//            //code = CommandExecutionCode.EXIT;
//        }
        if (!commandResult.getFirst()) {
            code = CommandExecutionCode.ERROR;
        }
        return new ServerResponse(code, commandResult.getSecond());
    }

    public CommandWrapper getCommandWrapper() {
        return commandWrapper;
    }
}

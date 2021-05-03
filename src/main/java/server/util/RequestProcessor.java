package server.util;

import server.Server;
import server.commands.abstracts.UserCommand;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;
import shared.serializable.ServerResponse;
import shared.util.CommandExecutionCode;

public class RequestProcessor {

    private CommandWrapper commandWrapper;

    public RequestProcessor(CommandWrapper commandWrapper) {
        this.commandWrapper = commandWrapper;
    }

    public ServerResponse processRequest(ClientRequest request) {

        Server.logger.info("Исполняется команда {}", request.getCommand());
        UserCommand userCommand = commandWrapper.getAllCommandsAvailable().get(request.getCommand());
        CommandExecutionCode code = CommandExecutionCode.SUCCESS;
        Pair<Boolean, String> commandResult = userCommand.execute(request.getCommandArgument(), request.getCreatedObject());

        if (!commandResult.getFirst()) {
            code = CommandExecutionCode.ERROR;
        } else {
            commandWrapper.updateHistory(userCommand);
        }

        if (request.getCommand().equals("exit")) {
            code = CommandExecutionCode.EXIT;
        }
        return new ServerResponse(code, commandResult.getSecond());
    }

    public CommandWrapper getCommandWrapper() {
        return commandWrapper;
    }
}

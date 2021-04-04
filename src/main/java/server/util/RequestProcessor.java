package server.util;

import server.commands.Command;
import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;

import java.util.Objects;

public class RequestProcessor {

    CommandWrapper commandWrapper;

    public RequestProcessor(CommandWrapper commandWrapper) {
        this.commandWrapper = commandWrapper;
    }

    public Pair<CommandExecutionCode, ServerResponse> processRequest(ClientRequest request) {

        Command command = commandWrapper.getAllCommandsAvailable().get(request.getCommand());
        CommandExecutionCode code = CommandExecutionCode.SUCCESS;
        Pair<Boolean, String> commandResult = command.execute(request.getCommandArgument(), request.getCreatedObject());
        if (command.getName().equals("exit")) {
            code = CommandExecutionCode.EXIT;
        } else {
            if (!commandResult.getFirst()) {
                code = CommandExecutionCode.ERROR;
            }
        }
        return new Pair<>(code, new ServerResponse(commandResult.getSecond()));
    }

//    private int commandExecutor(String[] command) {
//        int code = 1;
//        try {
//            String commandName = command[0].toLowerCase();
//            if (command[0].equals("") || getCommandWrapper().getAllCommandsAvailable().get(commandName).execute(command[1].trim())) {
//                if (!command[0].equals("")) {
//                    commandWrapper.updateHistory(getCommandWrapper().getAllCommandsAvailable().get(commandName));
//                }
//                if (Objects.equals(commandName, "exit")) {
//                    return -1;
//                }
//                if (Objects.equals(commandName, "execute_script")) {
//                    return scriptReader(command[1]);
//                }
//                return 0;
//            }
//        } catch (Exception e) {
//            return 1;
//        }
//        return code;
//    }

}

package server.util;


import server.commands.UserCommand;
import server.commands.InnerServerCommand;
import shared.serializable.Pair;

import java.util.HashMap;

/**
 * Класс-обертка для набора команд.
 * Привязан к командному режиму и используется в качестве посредника между ним и экземплярами команд.
 *
 */
public class CommandWrapper {

    private final HashMap<String, UserCommand> allCommandsAvailable = new HashMap<>();
    private final HashMap<String, InnerServerCommand> allInnerCommands = new HashMap<>();

    public CommandWrapper(CollectionStorage collectionStorage, UserCommand[] listOfUserCommands, InnerServerCommand[] innerServerCommands) {

        for (UserCommand userCommand : listOfUserCommands) {
            allCommandsAvailable.put(userCommand.getName(), userCommand);
            userCommand.setCommandWrapper(this);
            userCommand.setCollectionStorage(collectionStorage);
        }

        for (InnerServerCommand command : innerServerCommands) {
            allInnerCommands.put(command.getName(), command);
            command.setCommandWrapper(this);
            command.setCollectionStorage(collectionStorage);
        }
    }

    public HashMap<String, UserCommand> getAllCommandsAvailable() {
        return allCommandsAvailable;
    }

    public HashMap<String, InnerServerCommand> getAllInnerCommands() {
        return allInnerCommands;
    }

    public HashMap<String, Pair<String, Pair<Boolean, Boolean>>> mapOfCommandsToSend() {
        HashMap<String, Pair<String, Pair<Boolean, Boolean>>> mapToSend = new HashMap<>();
        UserCommand userCommand;
        for (String commandName: allCommandsAvailable.keySet()) {
            userCommand = allCommandsAvailable.get(commandName);
            mapToSend.put(commandName, new Pair<>(userCommand.getUtility(), new Pair<>(userCommand.isInteractive(), userCommand.hasStringArg())));
        }
        return mapToSend;
    }
}

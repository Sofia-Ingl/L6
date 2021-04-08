package server.util;


import server.commands.Command;
import shared.serializable.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Класс-обертка для набора команд.
 * Привязан к командному режиму и используется в качестве посредника между ним и экземплярами команд.
 *
 */
public class CommandWrapper {

    private final HashMap<String, Command> allCommandsAvailable = new HashMap<>();
    //private final ArrayList<Command> lastSixCommands = new ArrayList<>();

    public CommandWrapper(CollectionStorage collectionStorage, Command[] listOfCommands) {
        for (Command command : listOfCommands) {
            allCommandsAvailable.put(command.getName(), command);
            command.setCommandWrapper(this);
            command.setCollectionStorage(collectionStorage);
        }
    }

    public HashMap<String, Command> getAllCommandsAvailable() {
        return allCommandsAvailable;
    }


    public HashMap<String, Pair<String, Pair<Boolean, Boolean>>> mapOfCommandsToSend() {
        HashMap<String, Pair<String, Pair<Boolean, Boolean>>> mapToSend = new HashMap<>();
        Command command;
        for (String commandName: allCommandsAvailable.keySet()) {
            command = allCommandsAvailable.get(commandName);
            mapToSend.put(commandName, new Pair<>(command.getUtility(), new Pair<>(command.isInteractive(), command.hasStringArg())));
        }
        return mapToSend;
    }
}

package server.util;


import server.commands.Command;

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

}

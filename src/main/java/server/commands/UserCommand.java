package server.commands;

import server.util.CollectionStorage;
import server.util.CommandWrapper;

public abstract class UserCommand extends Command {

    public UserCommand(String name, String utility, boolean isInteractive, boolean hasStringArg) {
        super(name, utility, isInteractive, hasStringArg);
    }

}

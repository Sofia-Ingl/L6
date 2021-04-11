package server.commands;

import shared.serializable.Pair;

public abstract class InnerServerCommand extends UserCommand {


    public InnerServerCommand(String name, String utility) {
        super(name, utility, false, false);
    }
}

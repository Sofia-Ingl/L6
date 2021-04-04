package server.commands;

import server.util.CollectionStorage;
import server.util.CommandWrapper;

public abstract class Command implements Executables {
    private final String name;
    private final String utility;
    private CommandWrapper commandWrapper = null;
    private CollectionStorage collectionStorage = null;

    public Command(String name, String utility) {
        this.name = name;
        this.utility = utility;
    }

    public String getName() {
        return name;
    }

    public String getUtility() {
        return utility;
    }

    public void setCommandWrapper(CommandWrapper commandWrapper) {
        this.commandWrapper = commandWrapper;
    }

    public CommandWrapper getCommandWrapper() {
        return commandWrapper;
    }

    public CollectionStorage getCollectionStorage() {
        return collectionStorage;
    }

    public void setCollectionStorage(CollectionStorage collectionStorage) {
        this.collectionStorage = collectionStorage;
    }
}

package server.commands;

import server.util.CollectionStorage;
import server.util.CommandWrapper;

public abstract class Command implements Executables {
    private final String name;
    private final String utility;
    private final boolean isInteractive;
    private CommandWrapper commandWrapper = null;
    private CollectionStorage collectionStorage = null;

    public Command(String name, String utility, boolean isInteractive) {
        this.name = name;
        this.utility = utility;
        this.isInteractive = isInteractive;
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

    public boolean isInteractive() {
        return isInteractive;
    }
}

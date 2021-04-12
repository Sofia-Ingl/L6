package server.commands.abstracts;
import server.util.CollectionStorage;
import server.util.CommandWrapper;

public abstract class Command implements Executables {
    private final String name;
    private final String utility;
    private final boolean isInteractive;
    private final boolean hasStringArg;
    private CommandWrapper commandWrapper = null;
    private CollectionStorage collectionStorage = null;

    public Command(String name, String utility, boolean isInteractive, boolean hasStringArg) {
        this.name = name;
        this.utility = utility;
        this.isInteractive = isInteractive;
        this.hasStringArg = hasStringArg;
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

    public boolean hasStringArg() {
        return hasStringArg;
    }
}


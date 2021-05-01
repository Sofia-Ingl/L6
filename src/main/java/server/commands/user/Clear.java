package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.serializable.Pair;

public class Clear extends UserCommand {

    public Clear() {
        super("clear", "очистить коллекцию", false, false);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {
        synchronized (getCollectionStorage().getCollection()) {
            getCollectionStorage().clearCollection();
        }
        return new Pair<>(true, "Коллекция очищена");
    }
}

package server.commands;

import server.util.FileHelper;
import shared.serializable.Pair;

public class Save extends InnerServerCommand {

    public Save() {
        super("save", "сохранить коллекцию в файл");
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {
        boolean result = FileHelper.fileOutputLoader(getCollectionStorage().getCollection(), getCollectionStorage().getPath());
        return new Pair<>(result, "");
    }
}

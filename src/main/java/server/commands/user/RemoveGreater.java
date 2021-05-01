package server.commands.user;


import server.commands.abstracts.UserCommand;
import shared.data.Movie;
import shared.serializable.Pair;

/**
 * Команда, удаляющая элементы больше заданного.
 */
public class RemoveGreater extends UserCommand {

    public RemoveGreater() {
        super("remove_greater", "удалить из коллекции все элементы, превышающие заданный", true, false);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String response;
        Movie movieToCompareWith = (Movie) obj;
        boolean isDeleted;

        synchronized (getCollectionStorage().getCollection()) {
            isDeleted = getCollectionStorage().streamRemoveGreater(movieToCompareWith);
        }
        if (!isDeleted) {
            response = "В коллекции не было элементов, превосходящих заданный";
        } else {
            response = "Элементы больше заданного успешно удалены из коллекции";
        }
        return new Pair<>(true, response);
    }
}


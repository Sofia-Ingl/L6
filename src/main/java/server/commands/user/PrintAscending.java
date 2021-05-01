package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.data.Movie;
import shared.serializable.Pair;

import java.util.ArrayList;

/**
 * Команда, выводящая элементы коллекции по возрастанию.
 */
public class PrintAscending extends UserCommand {

    public PrintAscending() {
        super("print_ascending","вывести элементы коллекции в порядке возрастания", false, false);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        StringBuilder builder = new StringBuilder();
        synchronized (getCollectionStorage().getCollection()) {
            Pair<String, ArrayList<Movie>> collection = getCollectionStorage().getSortedCollection();
            builder.append(collection.getFirst()).append("\n");
            for (Movie movie : collection.getSecond()) {
                builder.append(movie).append("\n");
            }
        }

        return new Pair<>(true, builder.toString());
    }
}
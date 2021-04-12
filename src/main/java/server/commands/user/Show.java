package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.data.Movie;
import shared.serializable.Pair;

public class Show extends UserCommand {

    public Show() {
        super("show", " вывести в стандартный поток вывода все элементы коллекции в строковом представлении", false, false);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        StringBuilder responseString;
        String errorString;

        try {
            if (!arg.isEmpty()) {
                throw new IllegalArgumentException("Неверное число аргументов при использовании команды " + this.getName());
            }

            if (getCollectionStorage().getCollection().isEmpty()) {
                responseString = new StringBuilder("Коллекция пуста!");
            } else {
                responseString = new StringBuilder("В настоящий момент в коллекции находятся следующие элементы\n");
                for (Movie movie : getCollectionStorage().getSortedCollection().getSecond()) {
                    responseString.append(movie).append("\n");
                }
            }
            return new Pair<>(true, responseString.toString());

        } catch (IllegalArgumentException e) {
            errorString = e.getMessage();
        }

        return new Pair<>(false, errorString);
    }

}

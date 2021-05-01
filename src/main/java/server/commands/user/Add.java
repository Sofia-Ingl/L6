package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.data.Movie;
import shared.serializable.Pair;

public class Add extends UserCommand {

    public Add() {
        super("add", "добавить новый элемент в коллекцию", true, false);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String errorString;

        try {
            if (!arg.isEmpty()) {
                throw new IllegalArgumentException("Неверное число аргументов при использовании команды " + this.getName());
            }
            boolean result;

            synchronized (getCollectionStorage().getCollection()) {
                result = getCollectionStorage().addNewElement((Movie) obj);
            }

            if (result) {
                return new Pair<>(true, "Элемент добавлен в коллекцию!");
            }
            return new Pair<>(true, "Такой элемент уже был в коллекции");

        } catch (IllegalArgumentException e) {
            errorString = e.getMessage();
        }

        return new Pair<>(false, errorString);
    }
}

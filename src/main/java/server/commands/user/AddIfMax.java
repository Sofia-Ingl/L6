package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.data.Movie;
import shared.serializable.Pair;

public class AddIfMax extends UserCommand {

    public AddIfMax() {
        super("add_if_max", "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции", true, false);
    }


    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String response;
        Movie newMovie = (Movie) obj;

        if (getCollectionStorage().getMaxMovie() == null || getCollectionStorage().getMaxMovie().compareTo(newMovie) < 0) {
            boolean success = getCollectionStorage().addNewElement(newMovie);
            if (!success) {
                response = "Произошла коллизия, аналогичный элемент содержится в коллекции, поэтому заданный элемент не может быть добавлен";
            } else {
                response = "Предложенный вами фильм превосходит максимальный в коллекции => он будет добавлен.";
            }
        } else {
            response = "Предложеннй фильм максимальным не является => он не будет добавлен.";
        }
        return new Pair<>(true, response);
    }
}

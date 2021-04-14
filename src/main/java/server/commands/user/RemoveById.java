package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.serializable.Pair;

/**
 * Команда, удаляющая элементы по айди.
 */
public class RemoveById extends UserCommand {

    public RemoveById() {
        super("remove_by_id", "удалить элемент из коллекции по его id", false, true);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String response;
        try {
            if (!arg.trim().matches("\\d+")) {
                throw new IllegalArgumentException("Неправильный тип аргумента к команде!");
            }

            int id = Integer.parseInt(arg.trim());
            //boolean wasInCollection = getCollectionStorage().deleteElementForId(id);
            boolean wasInCollection = getCollectionStorage().streamDeleteElementForId(id);

            if (!wasInCollection) {
                response = "Нет элемента с таким значением id!";
            } else {
                response = "Элемент успешно удален";
            }

            return new Pair<>(true, response);

        } catch (NumberFormatException e) {
            response = "Неправильно введен аргумент!";
        } catch (IllegalArgumentException e) {
            response = e.getMessage();
        }
        return new Pair<>(false, response);
    }
}

package shared.exceptions;

/**
 * Исключение, которое бросается, если содержимое загружаемой из скрипта коллекции не соответствует критериям программы.
 *
 */
public class MalformedCollectionContentException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Содержимое файла не соответствует критериям программы => в загрузке отказано!\n" +
                "Рекомендуется проверить, корректно ли заданы значения полей объектов.";
    }
}

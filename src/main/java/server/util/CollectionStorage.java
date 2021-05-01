package server.util;

import server.Server;
import shared.data.Movie;
import shared.exceptions.MalformedCollectionContentException;
import shared.serializable.Pair;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс-обертка для коллекции, предназначенный для хранения коллекции и вспомогательной информации.
 * Предоставляет методы доступа и обработки, используется командами.
 */
public class CollectionStorage {
    private String path = null;
    private final Type collectionType = LinkedHashSet.class;
    private final Type contentType = Movie.class;

    private LinkedHashSet<Movie> collection = null;

    private LocalDateTime sortedCollectionUpdateTime = null;
    private ArrayList<Movie> sortedCollection = null;

    private LocalDateTime initTime = null;
    private LocalDateTime updateTime = null;
    private LocalDateTime lastAccessTime = null;

    private final ArrayList<Integer> allIds = new ArrayList<>();

    private Movie maxMovie = null;

    public String getPath() {
        return path;
    }

    /**
     * Метод, призванный загрузить коллекцию в хранилище-обертку.
     * Одновремнно представляет собой последний рубеж защиты от неправильного (не подходящего под критерии),
     * но корректного с точки зрения парсинга содержимого файла.
     * В этом случае метод выдает ошибку MalformedCollectionContentException, и в блоке ее обработки осуществляется выход
     * из программы.
     * Также выход происходит в случае, когда ошибки возникают еще на стадии парсинга.
     * Иными словами, правильная загрузка коллекции нужна для корректной работы приложения.
     *
     * @param fullPath путь к файлу
     */
    public void loadCollection(String fullPath) {
        try {
            path = fullPath;
            collection = FileHelper.jsonFileInputLoader(fullPath);
            for (Movie movie : collection) {
                if (movie.getName() == null || movie.getName().trim().equals("") || movie.getGoldenPalmCount() <= 0 ||
                        movie.getCoordinates() == null || movie.getCoordinates().getY() == null || movie.getCreationDate() == null ||
                        movie.getCoordinates().getY() > 281 || movie.getCoordinates().getX() > 326 ||
                        movie.getOscarsCount() <= 0 || movie.getTagline() == null || movie.getGenre() == null ||
                        movie.getScreenwriter() == null || movie.getScreenwriter().getName() == null ||
                        movie.getScreenwriter().getName().trim().equals("") || movie.getScreenwriter().getHeight() == null ||
                        movie.getScreenwriter().getHeight() < 0) {

                    throw new MalformedCollectionContentException();
                }
                if (!allIds.contains(movie.getId()) && movie.getId() > 0) {
                    allIds.add(movie.getId());
                } else {
                    throw new MalformedCollectionContentException();
                }
                if (maxMovie == null || maxMovie.compareTo(movie) < 0) {
                    maxMovie = movie;
                }
            }
            initTime = LocalDateTime.now();
            updateTime = initTime;
            lastAccessTime = updateTime;
            Server.logger.info("Коллекция успешно загружена!");

        } catch (NullPointerException e) {
            Server.logger.error("Коллекция не была успешно загружена...");
            Server.logger.error("Сервер завершает работу");
            System.exit(1);
        } catch (MalformedCollectionContentException e) {
            Server.logger.error(e.getMessage());
            Server.logger.error("Сервер завершает работу");
            System.exit(1);
        }
    }

    /**
     * Метод, возвращающий коллекцию.
     *
     * @return коллекция.
     */
    public LinkedHashSet<Movie> getCollection() {
        lastAccessTime = LocalDateTime.now();
        return collection;
    }

    /**
     * Метод, удаляющий элемент по айди.
     *
     * @param id айди, по которому следует удалить элемент.
     * @return произошло удаление или нет.
     */
    public boolean streamDeleteElementForId(int id) {

        lastAccessTime = LocalDateTime.now();
        if (allIds.contains(id)) {
            boolean ifWasMax = false;
            allIds.remove((Integer) id);
            if (maxMovie.getId() == id) {
                ifWasMax = true;
                maxMovie = null;
            }
            LinkedHashSet<Movie> newCollection = collection
                    .stream()
                    .filter(movie -> movie.getId() != id)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (ifWasMax) {
                setMaxMovie(newCollection);
            }
            collection = newCollection;
            updateTime = LocalDateTime.now();
            return true;
        }
        return false;
    }


    private void setMaxMovie(LinkedHashSet<Movie> set) {
        set.stream().max(Comparator.naturalOrder()).ifPresent(movie -> maxMovie = movie);
    }


    /**
     * Метод, возвращающий элемент по айди или null, если такой элемент не найден.
     *
     * @param id айди, элемент с которым надо вернуть.
     * @return Movie с заданным айди или null (если фильм не найден).
     */
    public Movie streamGetById(int id) {
        lastAccessTime = LocalDateTime.now();
        updateTime = lastAccessTime;
        if (allIds.contains(id)) {
            Optional<Movie> movie = collection.stream()
                    .filter(m -> m.getId() == id)
                    .findFirst();
            if (movie.isPresent()) {
                return movie.get();
            }
        }
        return null;
    }

    private int idGenerator() {
        return Math.abs(new Random().nextInt());
    }

    /**
     * Метод, добавляющий новый фильм в коллекцию.
     *
     * @param movie фильм, который надо добавить в коллекцию.
     */
    public boolean addNewElement(Movie movie) {

        synchronized (allIds) {
            int id;
            if (collection.contains(movie)) {
                return false;
            }
            do {
                id = idGenerator();
            } while (allIds.contains(id));
            movie.setId(id);
            allIds.add(id);
        }
        movie.setCreationDate(ZonedDateTime.now());
        collection.add(movie);
        updateTime = LocalDateTime.now();
        lastAccessTime = updateTime;


        if (maxMovie.compareTo(movie) < 0) {
            maxMovie = movie;
        }

        return true;
    }

    /**
     * Метод, удаляющий фильм(ы) из коллекции по имени сценариста.
     *
     * @param screenwriterName сценарист, по которому следует удалить элемент(ы).
     * @return произошло удаление или нет.
     */
    public boolean streamRemoveByScreenwriter(String screenwriterName) {
        final String screenwriter = screenwriterName.trim().toLowerCase();
        boolean maxDeleted = false;
        boolean isDeleted = false;
        if (screenwriter.matches(maxMovie.getScreenwriter().getName().trim().toLowerCase())) {
            maxDeleted = true;
            maxMovie = null;
        }
        allIds.clear();
        LinkedHashSet<Movie> newCollection = collection.stream()
                .filter(movie -> !movie.getScreenwriter().getName().trim().toLowerCase().equals(screenwriter))
                .peek(movie -> allIds.add(movie.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (newCollection.size() < collection.size()) {
            isDeleted = true;
            updateTime = LocalDateTime.now();
        }
        if (maxDeleted) {
            setMaxMovie(newCollection);
        }
        lastAccessTime = LocalDateTime.now();
        collection = newCollection;
        return isDeleted;
    }


    public Movie getMaxMovie() {
        return maxMovie;
    }

    public LocalDateTime getInitTime() {
        return initTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }


    /**
     * Метод, очищающий коллекцию.
     */
    public void clearCollection() {
        collection.clear();
        maxMovie = null;
        sortedCollection.clear();
        allIds.clear();
        updateTime = LocalDateTime.now();
        lastAccessTime = updateTime;
    }

    /**
     * Метод, возвращающий тип коллекции и тип хранимых в ней объектов.
     *
     * @return тип коллекции и тип хранимых в ней объектов.
     */
    public Type[] getTypes() {
        return new Type[]{collectionType, contentType};
    }


    public String streamReturnGreaterThanGoldenPalms(long goldenPalms) {

        lastAccessTime = LocalDateTime.now();
        StringBuilder builder = new StringBuilder();
        String heading = "Элементы, значение поля goldenPalmsCount у которых больше заданного\n";
        builder.append(heading);
        collection.stream()
                .filter(movie -> movie.getGoldenPalmCount() > goldenPalms)
                .forEach(movie -> builder.append(movie).append("\n"));
        if (builder.toString().equals(heading)) {
            return "В коллекции не было элементов, удовлетворяющих условию";
        }
        return builder.toString();
    }


    /**
     * Метод, удаляющий элементы большие, чем заданный.
     *
     * @param movie фильм, с которым будет производиться сравнение.
     * @return произошло хоть одно удаление или нет.
     */
    public boolean streamRemoveGreater(Movie movie) {
        if (maxMovie.compareTo(movie) > 0) {
            allIds.clear();
            LinkedHashSet<Movie> newCollection = collection.stream()
                    .filter(m -> m.compareTo(movie) <= 0)
                    .peek(m -> allIds.add(m.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            setMaxMovie(newCollection);
            collection = newCollection;
            updateTime = LocalDateTime.now();
            lastAccessTime = updateTime;
            return true;
        }
        return false;
    }

    public Pair<String, ArrayList<Movie>> getSortedCollection() {
        if (sortedCollection != null && sortedCollectionUpdateTime.isAfter(updateTime)) {
            return new Pair<>("Коллекция не обновлялась со времен последней сортировки", sortedCollection);
        } else {
            ArrayList<Movie> sortedCollection = new ArrayList<>(collection);
            lastAccessTime = LocalDateTime.now();
            sortedCollection.sort(Comparator.naturalOrder());
            this.sortedCollection = sortedCollection;
            sortedCollectionUpdateTime = LocalDateTime.now();
        }
        return new Pair<>("Коллекция обновилась со времен последней сортировки!", sortedCollection);
    }
}

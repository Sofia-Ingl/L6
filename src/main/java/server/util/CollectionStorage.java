package server.util;

import shared.data.Movie;
import shared.exceptions.MalformedCollectionContentException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

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
            System.out.println("Коллекция успешно загружена!");

        } catch (NullPointerException e) {
            System.out.println("Коллекция не была успешно загружена...");
            System.exit(1);
        } catch (MalformedCollectionContentException e) {
            System.out.println(e.getMessage());
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
    public boolean deleteElementForId(int id) {

        lastAccessTime = LocalDateTime.now();
        if (allIds.contains(id)) {
            boolean ifWasMax = false;
            allIds.remove((Integer) id);
            if (maxMovie.getId() == id) {
                maxMovie = null;
                ifWasMax = true;
            }
            Iterator<Movie> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Movie currentMovie = iterator.next();
                if (currentMovie.getId() == id) {
                    iterator.remove();
                    updateTime = LocalDateTime.now();
                } else {
                    if (ifWasMax && (maxMovie == null || currentMovie.compareTo(maxMovie) > 0)) {
                        maxMovie = currentMovie;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Метод, возвращающий элемент по айди или null, если такой элемент не найден.
     *
     * @param id айди, элемент с которым надо вернуть.
     * @return Movie с заданным айди или null (если фильм не найден).
     */
    public Movie getById(int id) {
        lastAccessTime = LocalDateTime.now();
        if (allIds.contains(id)) {
            for (Movie movie : collection) {
                if (movie.getId() == id) {
                    return movie;
                }
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
        int id;
        if (collection.contains(movie)) {
            return false;
        }
        do {
            id = idGenerator();
        } while (allIds.contains(id));
        movie.setId(id);
        allIds.add(id);
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
    public boolean removeByScreenwriter(String screenwriterName) {
        boolean isDeleted = false;
        boolean maxDeleted = false;
        if (screenwriterName.trim().toLowerCase().matches(maxMovie.getScreenwriter().getName().trim().toLowerCase())) {
            maxDeleted = true;
            maxMovie = null;
        }
        Iterator<Movie> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Movie currentMovie = iterator.next();
            if (screenwriterName.trim().toLowerCase().matches(currentMovie.getScreenwriter().getName().trim().toLowerCase())) {
                allIds.remove((Integer) currentMovie.getId());
                iterator.remove();
                isDeleted = true;
                updateTime = LocalDateTime.now();
            } else {
                if (maxDeleted && (maxMovie == null || currentMovie.compareTo(maxMovie) > 0)) {
                    maxMovie = currentMovie;
                }
            }
        }
        lastAccessTime = LocalDateTime.now();
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

    /**
     * Метод, печатающий в System.out элменты, значение поля goldenPalmsCount у которых больше заданного.
     *
     * @param goldenPalms число золотых пальмовых ветвей, с которым надо сравнить.
     */
    public void printGreaterThanGoldenPalms(long goldenPalms) {
        lastAccessTime = LocalDateTime.now();
        for (Movie movie : collection) {
            if (movie.getGoldenPalmCount() > goldenPalms) {
                System.out.println(movie);
            }
        }
    }

    public String returnGreaterThanGoldenPalms(long goldenPalms) {
        lastAccessTime = LocalDateTime.now();
        StringBuilder builder = new StringBuilder();
        builder.append("Элементы, значение поля goldenPalmsCount у которых больше заданного\n");
        boolean isAny = false;
        for (Movie movie : collection) {
            if (movie.getGoldenPalmCount() > goldenPalms) {
                //System.out.println(movie);
                isAny = true;
                builder.append(movie).append("\n");
            }
        }
        if (isAny) {
            return builder.toString();
        }
        return "В коллекции не было элементов, удовлетворяющих условию";
    }

    /**
     * Метод, выводящий в System.out элементы коллекции по возрастанию.
     */
    public void printAscending() {
        if (sortedCollection != null && sortedCollectionUpdateTime.isAfter(updateTime)) {
            System.out.println("Коллекция не обновлялась со времен последней сортировки");
        } else {
            System.out.println("Коллекция обновилась со времен последней сортировки!");
            ArrayList<Movie> sortedCollection = new ArrayList<>(collection);
            sortedCollection.sort(Comparator.naturalOrder());
            this.sortedCollection = sortedCollection;
            sortedCollectionUpdateTime = LocalDateTime.now();
        }

        for (Movie movie : this.sortedCollection) {
            System.out.println(movie);
        }
    }

    /**
     * Метод, удаляющий элементы большие, чем заданный.
     *
     * @param movie фильм, с которым будет производиться сравнение.
     * @return произошло хоть одно удаление или нет.
     */
    public boolean removeGreater(Movie movie) {
        Iterator<Movie> iterator = collection.iterator();
        Movie currentMovie;
        if (maxMovie.compareTo(movie) < 0) {
            return false;
        }
        maxMovie = null;
        while (iterator.hasNext()) {
            currentMovie = iterator.next();
            if (movie.compareTo(currentMovie) < 0) {
                allIds.remove((Integer) currentMovie.getId());
                iterator.remove();
                updateTime = LocalDateTime.now();
            } else {
                if (maxMovie == null || currentMovie.compareTo(maxMovie) > 0) {
                    maxMovie = currentMovie;
                }
            }
        }
        lastAccessTime = LocalDateTime.now();
        return true;

    }

    public ArrayList<Movie> getSortedCollection() {
        if (sortedCollection != null && sortedCollectionUpdateTime.isAfter(updateTime)) {
            return sortedCollection;
        } else {
            //System.out.println("Коллекция обновилась со времен последней сортировки!");
            ArrayList<Movie> sortedCollection = new ArrayList<>(collection);
            sortedCollection.sort(Comparator.naturalOrder());
            this.sortedCollection = sortedCollection;
            sortedCollectionUpdateTime = LocalDateTime.now();
        }
        return sortedCollection;
    }
}

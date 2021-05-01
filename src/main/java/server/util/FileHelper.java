package server.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import server.Server;
import shared.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Scanner;

/**
 * Утилитарный класс для работы с файлами.
 *
 */
public class FileHelper {

    private final static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Метод, выгружающий коллекцию из файла в формате json.
     *
     * @param fullPath путь к файлу.
     *
     * @return коллекция или null (в случае неуспеха).
     */
    public static LinkedHashSet<Movie> jsonFileInputLoader(String fullPath) {
        File file = new File(fullPath);
        Path p = Paths.get(fullPath);
        try {
            if (p.toRealPath().toString().length() > 3 && p.toRealPath().toString().trim().startsWith("/dev")) {
                Server.logger.error("Недопустимый путь к файлу!");
                return null;
            }
        } catch (IOException | SecurityException e) {
            Server.logger.error("Файл не найден или не хватает прав доступа");
            return null;
        }
        try (FileInputStream inputStream = new FileInputStream(file);
             BufferedInputStream bufferedIS = new BufferedInputStream(inputStream);
             Scanner scanner = new Scanner(bufferedIS))
        {
            if (scanner.hasNext()) {
                return fromJsonToObjects(scanner.nextLine().trim());
            }
        } catch (FileNotFoundException e) {
            Server.logger.error("Файл не найден!");
        } catch (IOException e) {
            Server.logger.error("Ошибка ввода-вывода.");
        } catch (IllegalStateException e) {
            Server.logger.error("Сканнер закрылся вместе с потоком?");
            //System.exit(1);
        } catch (SecurityException e) {
            Server.logger.error("Ограниченные права доступа не позволяют осуществить чтение из файла.");
        } catch (JSONException | JsonParseException e) {
            Server.logger.error("Ошибка парсинга json.");
        }
        return null;
    }

    /**
     * Метод, записывающий коллекцию в файл в формате json.
     *
     * @param collection коллекция.
     * @param path путь к файлу.
     *
     * @return все прошло успешно или нет.
     */
    public static boolean fileOutputLoader(LinkedHashSet<Movie> collection, String path) {
        try (PrintWriter printWriter = new PrintWriter(path)) {
            printWriter.write(fromObjectsToJson(collection));
            return true;
        } catch (FileNotFoundException e) {
            Server.logger.error("Файл не найден!");
        } catch (SecurityException e) {
            Server.logger.error("Ограниченные права доступа не позволяют осуществить запись в файл.");
        } catch (JSONException | JsonParseException e) {
            Server.logger.error("Ошибка парсинга json.");
        }
        return false;
    }

    private static LinkedHashSet<Movie> fromJsonToObjects(String s) {
        JSONArray array = new JSONArray(s);
        LinkedHashSet<Movie> collection = new LinkedHashSet<>();
        Movie movie;
        JSONObject jo;
        for (Object object : array) {
            movie = gson.fromJson(object.toString(), Movie.class);
            jo = (JSONObject) object;
            movie.setCreationDate(ZonedDateTime.parse(jo.get("creationDate").toString()));
            collection.add(movie);
        }
        return collection;
    }

    private static String fromObjectsToJson(LinkedHashSet<Movie> collection) {
        StringBuilder jsonString = new StringBuilder("[");
        JSONObject jo;
        for (Movie movie : collection) {
            String movieString = gson.toJson(movie);
            jo = new JSONObject(movieString);
            jo.put("creationDate", movie.getCreationDate().toString());
            movieString = jo.toString();
            jsonString.append(movieString).append(", ");
        }
        return jsonString.substring(0, jsonString.length() - 2) + "]";
    }

}
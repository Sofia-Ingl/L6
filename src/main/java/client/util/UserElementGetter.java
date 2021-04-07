package client.util;


import shared.data.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * Класс, реализующий опрос пользователя и создание экземпляров класса Movie на основе полученной информации.
 *
 */
public class UserElementGetter extends InteractiveConsoleUtils {

    public UserElementGetter() {}

    public UserElementGetter(InputStream inputStream, OutputStream outputStream) {
        setIn(inputStream);
        setScanner(new Scanner(inputStream));
        setOut(outputStream);
    }


    /**
     * Интерактивный метод, получающий посредством опроса пользователя значения всех нестатических полей класса Movie.
     * На их основе создает и возвращает новый экземпляр.
     * Обладает встроенными проверками корректности значений каждого поля.
     *

     * @return экземпляр Movie, созданный на основе пользовательского ввода.
     */
    public Movie movieGetter() {
        if (getScanner()!=null) {
            writeMessage("Пожалуйста, введите значения полей, характеризующих новый фильм.\n");
            return new Movie(nameGetter(false), coordinatesGetter(), oscarsCountGetter(), heightOrPalmsCountGetter(false),
                    taglineGetter(), genreGetter(), screenwriterGetter());
        } else {
            System.out.println("Сканнер у UserElementGetter не инициализирован!");
            return null;
        }

    }

    private String nameGetter(boolean isScreenwriter) {

        if (isScreenwriter) {
            writeMessage("Введите имя:\n");
        } else {
            writeMessage("Введите название фильма:\n");
        }
        String line;
        do {
            writeMessage(">>");
            line = getScanner().nextLine().trim();
            if (line.isEmpty()) {
                writeMessage("Строка не должна быть пустой!\n");
            }
        } while (line.isEmpty());

        return line.substring(0, 1).toUpperCase() + line.substring(1);
    }

    private Coordinates coordinatesGetter() {
        writeMessage("Введите координаты x и y через пробел (первое число может быть дробным и не больше 326, второе целым и не больше 281):\n");
        String[] xAndY;
        boolean exceptions;
        Coordinates coordinates = null;
        do {
            exceptions = false;
            writeMessage(">>");
            xAndY = getScanner().nextLine().trim().concat(" ").split(" ", 2);
            try {
                coordinates = new Coordinates(Float.parseFloat(xAndY[0].trim()), Integer.parseInt(xAndY[1].trim()));
                if (coordinates.getX()>326 || coordinates.getY()>281) {
                    writeMessage("Координаты не должны превосходить заданных значений!\n");
                    exceptions = true;
                }
            } catch (NumberFormatException e) {
                exceptions = true;
                writeMessage("Некорректный ввод! Повторите попытку.\n");
            }
        } while (exceptions);
        return coordinates;
    }

    private int oscarsCountGetter() {
        writeMessage("Введите количество оскаров (их число целое, больше 0 и не больше максимального интеджера):\n");
        int oscars = 0;
        boolean exceptions;
        do {
            exceptions = false;
            writeMessage(">>");
            try {
                oscars = Integer.parseInt(getScanner().nextLine().trim());
                if (oscars < 1) {
                    writeMessage("Число должно быть строго положительным!\n");
                    exceptions = true;
                }
            } catch (NumberFormatException e) {
                exceptions = true;
                writeMessage("Некорректный ввод! Повторите попытку.\n");
            }
        } while (exceptions);
        return oscars;
    }

    private long heightOrPalmsCountGetter(boolean isScreenwriter) {
        if (isScreenwriter) {
            writeMessage("Введите рост:\n");
        } else {
            writeMessage("Введите количество золотых пальмовых ветвей (их число целое, больше 0 и вмещается в лонг)\n");
        }
        long palmsOrHeight = 0;
        boolean exceptions;
        do {
            exceptions = false;
            writeMessage(">>");
            try {
                palmsOrHeight = Long.parseLong(getScanner().nextLine().trim());
                if (palmsOrHeight < 1) {
                    writeMessage("Число должно быть строго положительным!\n");
                    exceptions = true;
                }
            } catch (NumberFormatException e) {
                exceptions = true;
                writeMessage("Некорректный ввод! Повторите попытку.\n");
            }
        } while (exceptions);
        return palmsOrHeight;
    }

    private String taglineGetter() {
        writeMessage("Введите строку тегов:\n");
        writeMessage(">>");
        return getScanner().nextLine().trim();
    }

    private MovieGenre genreGetter() {

        writeMessage("Введите жанр фильма:\n");
        writeMessage("(Доступные жанры: " + enumContentGetter(MovieGenre.class) + ")\n");

        MovieGenre genre = null;
        boolean exceptions;
        do {
            exceptions = false;
            writeMessage(">>");
            try {
                genre = MovieGenre.valueOf(getScanner().nextLine().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                exceptions = true;
                writeMessage("Неверно введена константа! Повторите попытку\n");
            }
        } while (exceptions);

        return genre;
    }

    private Person screenwriterGetter() {
        writeMessage("Сейчас вам будет предложено описать сценариста фильма.\n");
        return new Person(nameGetter(true), heightOrPalmsCountGetter(true), eyeColorGetter(), nationalityGetter());
    }

    @SuppressWarnings("rawtypes")
    private String enumContentGetter(Class<? extends Enum> clazz) {
        StringBuilder description = new StringBuilder();
        try {
            Enum[] constants = clazz.getEnumConstants();
            for (Enum m : constants) {
                description.append(m.name()).append(", ");
            }
            description = new StringBuilder(description.substring(0, description.length() - 2));
        } catch (Exception e) {
            System.out.println("Шо-то пошло не так, а шо - непонятно :с");
        }
        return description.toString();
    }

    private Color eyeColorGetter() {
        writeMessage("Введите цвет глаз:\n");
        writeMessage("(Доступные цвета: " + enumContentGetter(Color.class) + ")\n");
        Color color = null;
        String colorString;
        boolean exceptions;
        do {
            exceptions = false;
            writeMessage(">>");
            try {
                colorString = getScanner().nextLine().trim().toUpperCase();
                if (!colorString.equals("")) {
                    color = Color.valueOf(colorString);
                }
            } catch (IllegalArgumentException e) {
                exceptions = true;
                writeMessage("Неверно введена константа! Повторите попытку\n");
            }
        } while (exceptions);

        return color;
    }

    private Country nationalityGetter() {
        writeMessage("Введите национальную принадлежность:\n");
        writeMessage("(Доступные страны: " + enumContentGetter(Country.class) + ")\n");
        Country country = null;
        String nationality;
        boolean exceptions;
        do {
            exceptions = false;
            writeMessage(">>");
            try {
                nationality = getScanner().nextLine().trim().toUpperCase();
                if (!nationality.equals("")) {
                    country = Country.valueOf(nationality);
                }
            } catch (IllegalArgumentException e) {
                exceptions = true;
                writeMessage("Неверно введена константа! Повторите попытку\n");
            }
        } while (exceptions);
        return country;
    }
}

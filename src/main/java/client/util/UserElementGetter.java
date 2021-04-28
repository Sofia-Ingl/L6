package client.util;


import shared.data.*;


/**
 * Класс, реализующий опрос пользователя и создание экземпляров класса Movie на основе полученной информации.
 */
public class UserElementGetter extends InteractiveConsoleUtils {

    public UserElementGetter() {}


    /**
     * Интерактивный метод, получающий посредством опроса пользователя значения всех нестатических полей класса Movie.
     * На их основе создает и возвращает новый экземпляр.
     * Обладает встроенными проверками корректности значений каждого поля.
     *
     * @return экземпляр Movie, созданный на основе пользовательского ввода.
     */
    public Movie movieGetter() {
        if (getScanner() != null) {
            printlnMessage("Пожалуйста, введите значения полей, характеризующих новый фильм.");
            return new Movie(nameGetter(false), coordinatesGetter(), oscarsCountGetter(), heightOrPalmsCountGetter(false),
                    taglineGetter(), genreGetter(), screenwriterGetter());
        } else {
            printlnMessage("Сканнер у UserElementGetter не инициализирован!");
            return null;
        }

    }

    private String nameGetter(boolean isScreenwriter) {

        if (isScreenwriter) {
            printlnMessage("Введите имя:");
        } else {
            printlnMessage("Введите название фильма:");
        }
        String line;
        do {
            printMessage(">>");
            systemInClosedProcessing();
            line = getScanner().nextLine().trim();
            if (line.isEmpty()) {
                printlnMessage("Строка не должна быть пустой!");
            }
        } while (line.isEmpty());

        return line.substring(0, 1).toUpperCase() + line.substring(1);
    }

    private Coordinates coordinatesGetter() {
        printlnMessage("Введите координаты x и y через пробел (первое число может быть дробным и не больше 326, второе целым и не больше 281):");
        String[] xAndY;
        boolean exceptions;
        Coordinates coordinates = null;
        do {
            exceptions = false;
            printMessage(">>");
            systemInClosedProcessing();
            xAndY = getScanner().nextLine().trim().concat(" ").split(" ", 2);
            try {
                coordinates = new Coordinates(Float.parseFloat(xAndY[0].trim()), Integer.parseInt(xAndY[1].trim()));
                if (coordinates.getX() > 326 || coordinates.getY() > 281) {
                    printlnMessage("Координаты не должны превосходить заданных значений!");
                    exceptions = true;
                }
            } catch (NumberFormatException e) {
                exceptions = true;
                printlnMessage("Некорректный ввод! Повторите попытку.");
            }
        } while (exceptions);
        return coordinates;
    }

    private int oscarsCountGetter() {
        printlnMessage("Введите количество оскаров (их число целое, больше 0 и не больше максимального интеджера):");
        int oscars = 0;
        boolean exceptions;
        do {
            exceptions = false;
            printMessage(">>");
            try {
                systemInClosedProcessing();
                oscars = Integer.parseInt(getScanner().nextLine().trim());
                if (oscars < 1) {
                    printlnMessage("Число должно быть строго положительным!");
                    exceptions = true;
                }
            } catch (NumberFormatException e) {
                exceptions = true;
                printlnMessage("Некорректный ввод! Повторите попытку.");
            }
        } while (exceptions);
        return oscars;
    }

    private long heightOrPalmsCountGetter(boolean isScreenwriter) {
        if (isScreenwriter) {
            printlnMessage("Введите рост:");
        } else {
            printlnMessage("Введите количество золотых пальмовых ветвей (их число целое, больше 0 и вмещается в лонг)");
        }
        long palmsOrHeight = 0;
        boolean exceptions;
        do {
            exceptions = false;
            printMessage(">>");
            try {
                systemInClosedProcessing();
                palmsOrHeight = Long.parseLong(getScanner().nextLine().trim());
                if (palmsOrHeight < 1) {
                    printlnMessage("Число должно быть строго положительным!");
                    exceptions = true;
                }
            } catch (NumberFormatException e) {
                exceptions = true;
                printlnMessage("Некорректный ввод! Повторите попытку.");
            }
        } while (exceptions);
        return palmsOrHeight;
    }

    private String taglineGetter() {
        printlnMessage("Введите строку тегов:");
        printMessage(">>");
        systemInClosedProcessing();
        return getScanner().nextLine().trim();
    }

    private MovieGenre genreGetter() {

        printlnMessage("Введите жанр фильма:");
        printlnMessage("(Доступные жанры: " + enumContentGetter(MovieGenre.class) + ")");

        MovieGenre genre = null;
        boolean exceptions;
        do {
            exceptions = false;
            printMessage(">>");
            try {
                systemInClosedProcessing();
                genre = MovieGenre.valueOf(getScanner().nextLine().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                exceptions = true;
                printlnMessage("Неверно введена константа! Повторите попытку");
            }
        } while (exceptions);

        return genre;
    }

    private Person screenwriterGetter() {
        printlnMessage("Сейчас вам будет предложено описать сценариста фильма.");
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
            printlnMessage("Шо-то пошло не так, а шо - непонятно :с");
        }
        return description.toString();
    }

    private Color eyeColorGetter() {
        printlnMessage("Введите цвет глаз:");
        printlnMessage("(Доступные цвета: " + enumContentGetter(Color.class) + ")");
        Color color = null;
        String colorString;
        boolean exceptions;
        do {
            exceptions = false;
            printMessage(">>");
            try {

                systemInClosedProcessing();
                colorString = getScanner().nextLine().trim().toUpperCase();
                if (!colorString.equals("")) {
                    color = Color.valueOf(colorString);
                }
            } catch (IllegalArgumentException e) {
                exceptions = true;
                printlnMessage("Неверно введена константа! Повторите попытку");
            }
        } while (exceptions);

        return color;
    }

    private Country nationalityGetter() {
        printlnMessage("Введите национальную принадлежность:");
        printlnMessage("(Доступные страны: " + enumContentGetter(Country.class) + ")");
        Country country = null;
        String nationality;
        boolean exceptions;
        do {
            exceptions = false;
            printMessage(">>");
            try {
                systemInClosedProcessing();
                nationality = getScanner().nextLine().trim().toUpperCase();
                if (!nationality.equals("")) {
                    country = Country.valueOf(nationality);
                }
            } catch (IllegalArgumentException e) {
                exceptions = true;
                printlnMessage("Неверно введена константа! Повторите попытку");
            }
        } while (exceptions);
        return country;
    }

    private void systemInClosedProcessing() {
        if (getScanner() == null || !getScanner().hasNextLine()) {
            printlnMessage("\nПоток ввода завершен, приложение будет закрыто");
            System.exit(0);
        }
    }
}

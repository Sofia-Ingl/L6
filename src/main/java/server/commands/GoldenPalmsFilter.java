package server.commands;

import shared.serializable.Pair;

public class GoldenPalmsFilter extends Command {

    public GoldenPalmsFilter() {
        super("filter_greater_than_golden_palm_count","вывести элементы, значение поля goldenPalmCount которых больше заданного", false, true);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String errorString;

        try {
            if (arg.isEmpty()) {
                throw new IllegalArgumentException("Неверное число аргументов при использовании команды " + this.getName());
            }
//            if (getCollectionStorage() == null) {
//                throw new InappropriateProgramConfigurationException();
//            }
            if (!arg.trim().matches("\\d+")) {
                throw new IllegalArgumentException("Неправильный тип аргумента к команде!");
            } else {
                long goldenPalms = Long.parseLong(arg.trim());
                String result = getCollectionStorage().returnGreaterThanGoldenPalms(goldenPalms);
                return new Pair<>(true, result);

            }

        } catch (NumberFormatException e) {
            errorString = "Неправильно введен аргумент!";
            //System.out.println("Неправильно введен аргумент!");
        } catch (IllegalArgumentException e) {
            errorString = e.getMessage();
            //System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            errorString = "Команда не привязана к хранилищу коллекции!";
            //System.out.println("Команда не привязана к хранилищу коллекции!");
        }
//        catch (InappropriateProgramConfigurationException e) {
//            System.out.println(e.getMessage());
//            System.exit(1);
//        }
        return new Pair<>(false, errorString);
    }
}


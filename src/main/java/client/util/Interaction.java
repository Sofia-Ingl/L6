package client.util;

import shared.data.Movie;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Interaction {

    private final Scanner defaultScanner = new Scanner(System.in);
    private Scanner currentScriptScanner = null;
    private UserElementGetter userElementGetter;
    /**
     * HashMap<String, Pair<String, Pair<Boolean, Boolean>>> commandsAvailable
     * Ключ - название команды
     * Значение - пара, где 1 элемент - описание команды, второй элмент - пара булевых значений;
     * Первое из них говорит, интерактивна ли команда. Второе - принимает ли она строчной аргумент.
     */
    private HashMap<String, Pair<String, Pair<Boolean, Boolean>>> commandsAvailable;
    private boolean isScript = false;
    private Stack<Pair<Path, Scanner>> scriptsWithScanners = new Stack<>();


    public Interaction(UserElementGetter userElementGetter) {
        //this.scanner = scanner;
        this.userElementGetter = userElementGetter;

        this.userElementGetter.setIn(System.in);
        this.userElementGetter.setOut(System.out);
        this.userElementGetter.setScanner(defaultScanner);
    }

    public ClientRequest formRequest() {

        if (!isScript) {

            String[] commandWithArg;
            Movie movie = null;
            String command = "";
            String commandArg = "";
            boolean validation = false;

            while (!validation) {
                System.out.print(">");
                commandWithArg = (defaultScanner.nextLine() + " ").split(" ", 2);
                command = commandWithArg[0].trim();
                commandArg = commandWithArg[1].trim();
                validation = commandIsValid(command, commandArg);
                if (!validation) {
                    System.out.println("Команды с таким именем нет!");
                }
                System.out.println(validation);
                System.out.println(commandsAvailable.get(command));
            }

            if (commandsAvailable.get(command).getSecond().getFirst()) {
                movie = userElementGetter.movieGetter();
            }

//            if (command.equals("execute_script")) {
//
//            }

            return new ClientRequest(command, commandArg, movie);

        }
//        } else {
//
//
//        }
        return null;
    }

    private void setScriptSettings() {
        isScript = true;
    }


    private boolean commandIsValid(String command, String commandArg) {
        Pair<String, Pair<Boolean, Boolean>> commandInfo = commandsAvailable.get(command);
        if (commandInfo == null) return false;
        boolean stringArgValidation = false;
        if ((commandInfo.getSecond().getSecond() && !commandArg.equals("")) || (!commandInfo.getSecond().getSecond() && commandArg.equals(""))) {
            stringArgValidation = true;
        }
        return stringArgValidation;
    }


    public void setCommandsAvailable(HashMap<String, Pair<String, Pair<Boolean, Boolean>>> commandsAvailable) {
        this.commandsAvailable = commandsAvailable;
    }

    public HashMap<String, Pair<String, Pair<Boolean, Boolean>>> getCommandsAvailable() {
        return commandsAvailable;
    }
}

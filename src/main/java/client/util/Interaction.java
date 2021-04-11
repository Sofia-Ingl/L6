package client.util;

import shared.data.Movie;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;
import shared.util.CommandExecutionCode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Interaction {

    private final Scanner defaultScanner = new Scanner(System.in);
    private Scanner currentScriptScanner = null;
    private final UserElementGetter userElementGetter;
    /**
     * HashMap<String, Pair<String, Pair<Boolean, Boolean>>> commandsAvailable
     * Ключ - название команды
     * Значение - пара, где 1 элемент - описание команды, второй элмент - пара булевых значений;
     * Первое из них говорит, интерактивна ли команда. Второе - принимает ли она строчной аргумент.
     */
    private HashMap<String, Pair<String, Pair<Boolean, Boolean>>> commandsAvailable = null;
    private boolean isScript = false;
    private final Stack<Path> files = new Stack<>();
    private final Stack<Scanner> scanners = new Stack<>();
    //private Stack<Pair<Path, Scanner>> scriptsWithScanners = new Stack<>();


    public Interaction(UserElementGetter userElementGetter) {
        //this.scanner = scanner;
        this.userElementGetter = userElementGetter;

        this.userElementGetter.setIn(System.in);
        this.userElementGetter.setOut(System.out);
        this.userElementGetter.setScanner(defaultScanner);
    }

    public ClientRequest formRequest(CommandExecutionCode code) {

        String[] commandWithArg;
        Movie movie = null;
        String command = "";
        String commandArg = "";
        boolean validation = false;

        if (!isScript) {

            while (!validation) {
                System.out.print(">");
                commandWithArg = (defaultScanner.nextLine() + " ").split(" ", 2);
                command = commandWithArg[0].trim();
                commandArg = commandWithArg[1].trim();
                validation = commandIsValid(command, commandArg);
                if (!validation) {
                    System.out.println("Команды с таким именем нет!");
                }
                //System.out.println(commandsAvailable.get(command));
            }

            if (commandsAvailable.get(command).getSecond().getFirst()) {
                movie = userElementGetter.movieGetter();
            }

            if (command.equals("execute_script")) {
                boolean success = putScriptOnStack(commandArg);
                if (!success) {
                    return null;
                }
            }

        } else {

            if (code == CommandExecutionCode.ERROR) {
                // ошибка внутри скрипта, обнаруженная на сервере, выходим из всех скриптов removeAllFromStack
                removeAllFromStack();
                return null;
            }
            while (!currentScriptScanner.hasNextLine()) {
                removeLastFromStack();
                if (!isScript) {
                    return null;
                }
                // снимаем скрипты со стека removeLastFromStack
                // если снят последний скрипт и установлен isScript = false, возвращаем null
            }

            commandWithArg = (currentScriptScanner.nextLine() + " ").split(" ", 2);
            command = commandWithArg[0].trim();
            commandArg = commandWithArg[1].trim();
            validation = commandIsValid(command, commandArg);
            if (!validation) {
                System.out.println("В скрипте обнаружена ошибка!");
                // ошибка внутри скрипта, removeAllFromStack
                removeAllFromStack();
                return null;
            }

            System.out.print(">");
            System.out.println(command + " " + commandArg);

            if (commandsAvailable.get(command).getSecond().getFirst()) {
                movie = userElementGetter.movieGetter();
            }

            if (command.equals("execute_script")) {
                boolean success = putScriptOnStack(commandArg);
                if (!success) {
                    removeAllFromStack();
                    // ошибка в скрипте, выходим - removeAllFromStack
                    return null;
                }
            }

            if (command.equals("exit")) {
                removeAllFromStack();
            }

        }

        return new ClientRequest(command, commandArg, movie);
    }

    private boolean putScriptOnStack(String path) {

        try {

            Path realPath = Paths.get(path).toRealPath();
            if (realPath.toString().length() > 3 && realPath.toString().trim().startsWith("/dev")) {
                System.out.println("Пошалить вздумал?) Не в мою смену, братишка!");
                return false;
            }

            //Pair<Path, Scanner> script = new Pair<>(realPath, new Scanner(realPath));
            // ПРОВЕРКА РЕКУРСИИ
            if (files.contains(realPath)) {
                System.out.println("РЕКУРСИЯ В СКРИПТЕ!!!");
                return false;
            }

            // НАСТРОЙКИ ЕСЛИ В СКРИПТЕ НЕТ РЕКУРСИИ И ПРОЧИХ ПОДСТАВ
            isScript = true;
            currentScriptScanner = new Scanner(realPath);

            files.push(realPath);
            scanners.push(currentScriptScanner);

            userElementGetter.setScanner(currentScriptScanner);
            userElementGetter.setScriptReader(true);
            //
            // НЕ ВСЕ ДОПИСАНО!
            //
            return true;

        } catch (IOException | SecurityException | IllegalArgumentException e) {
            System.out.println("Ой ффсе! Кажется, кто-то подсунул паленый скрипт :c Признавайся, чертяка, это ты сделал?!");
            //e.printStackTrace();
        }
        return false;
    }

    private void removeLastFromStack() {
        Scanner scannerToClose = scanners.pop();
        scannerToClose.close();
        System.out.println("Выход из скрипта " + files.pop().toString());
        if (scanners.isEmpty()) {
            isScript = false;
            currentScriptScanner = null;
            userElementGetter.setScanner(defaultScanner);
            userElementGetter.setScriptReader(false);
        } else {
            currentScriptScanner = scanners.peek();
            userElementGetter.setScanner(currentScriptScanner);
        }
    }

    public void removeAllFromStack() {
        Scanner scannerToClose;
        while (!scanners.isEmpty()) {
            scannerToClose = scanners.pop();
            scannerToClose.close();
        }
        isScript = false;
        currentScriptScanner = null;
        userElementGetter.setScanner(defaultScanner);
        userElementGetter.setScriptReader(false);
        files.clear();
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

    public String readLine() {
        return defaultScanner.nextLine();
    }
}

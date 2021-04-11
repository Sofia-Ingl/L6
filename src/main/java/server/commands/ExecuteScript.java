package server.commands;

import shared.serializable.Pair;

/**
 * Команда, исполняющая скрипт.
 */
public class ExecuteScript extends UserCommand {

    public ExecuteScript() {
        super("execute_script", "считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.", false, true);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String errorString;

        try {
            if (arg.isEmpty()) {
                throw new IllegalArgumentException("Неверное число аргументов при использовании команды " + this.getName());
            }
            return new Pair<>(true, "Сервер готов к исполнению команд из скрипта " + arg);

        } catch (IllegalArgumentException e) {
            //System.out.println(e.getMessage());
            errorString = e.getMessage();
        }
        return new Pair<>(false, errorString);
    }
}

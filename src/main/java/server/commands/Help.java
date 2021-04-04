package server.commands;

import server.util.Pair;

public class Help extends Command {

    private String commandInfo = null;

    public Help() {
        super("help", "вывести справку по доступным командам");
    }


    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String errorString;

        try {
            if (!arg.isEmpty()) {
                throw new IllegalArgumentException("Неверное число аргументов при использовании команды " + this.getName());
            }
            if (commandInfo == null) {
                String help = "\n" + "ИНФОРМАЦИЯ О ДОСТУПНЫХ КОМАНДАХ" + "\n";
                for (Command command : getCommandWrapper().getAllCommandsAvailable().values()) {
                    help = help + command.getName() + " ~> " + command.getUtility() + "\n";
                }
                commandInfo = help;
            }
            return new Pair<>(true, commandInfo);

        } catch (IllegalArgumentException e) {
            errorString = e.getMessage();
        }

        return new Pair<>(false, errorString);
    }
}

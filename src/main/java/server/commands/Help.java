package server.commands;

import shared.serializable.Pair;

public class Help extends Command {

    private String commandInfo = null;

    public Help() {
        super("help", "вывести справку по доступным командам", false);
    }


    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        String errorString;

        try {
            if (!arg.isEmpty()) {
                throw new IllegalArgumentException("Неверное число аргументов при использовании команды " + this.getName());
            }
            if (commandInfo == null) {
                StringBuilder help = new StringBuilder("\n" + "ИНФОРМАЦИЯ О ДОСТУПНЫХ КОМАНДАХ" + "\n");
                for (Command command : getCommandWrapper().getAllCommandsAvailable().values()) {
                    help.append(command.getName()).append(" ~> ").append(command.getUtility()).append("\n");
                }
                commandInfo = help.toString();
            }
            return new Pair<>(true, commandInfo);

        } catch (IllegalArgumentException e) {
            errorString = e.getMessage();
        }

        return new Pair<>(false, errorString);
    }
}

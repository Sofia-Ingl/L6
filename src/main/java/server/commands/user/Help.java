package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.serializable.Pair;

public class Help extends UserCommand {

    private String commandInfo = null;

    public Help() {
        super("help", "вывести справку по доступным командам", false, false);
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
                for (UserCommand userCommand : getCommandWrapper().getAllCommandsAvailable().values()) {
                    help.append(userCommand.getName()).append(" ~> ").append(userCommand.getUtility()).append("\n");
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

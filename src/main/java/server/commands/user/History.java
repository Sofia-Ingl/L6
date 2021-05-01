package server.commands.user;

import server.commands.abstracts.Command;
import server.commands.abstracts.UserCommand;
import shared.serializable.Pair;

import java.util.ArrayList;

public class History extends UserCommand {

    public History() {
        super("history", "вывести последние 6 команд (без их аргументов)", false, false);
    }


    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        StringBuilder builder = new StringBuilder();
        synchronized (getCommandWrapper().getHistory()) {
            ArrayList<Command> history = getCommandWrapper().getHistory();
            if (history.size() == 0) {
                builder.append("История команд пуста.");
            } else {
                builder.append("\nИСТОРИЯ (ПОСЛЕДНИЕ 6 КОМАНД, ОТ НОВЫХ К СТАРЫМ)\n");
                for (int index = 0; index < history.size(); index++) {
                    builder.append(history.get(history.size() - 1 - index).getName()).append("\n");
                }
            }
        }

        return new Pair<>(true, builder.toString());
    }
}

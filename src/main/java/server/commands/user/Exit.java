package server.commands.user;

import server.commands.abstracts.UserCommand;
import shared.serializable.Pair;

public class Exit extends UserCommand {

    public Exit() {
        super("exit", "завершить программу (без сохранения в файл)", false, false);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {

        return new Pair<>(true, "Клиент завершил работу с помощью команды exit.");

    }
}


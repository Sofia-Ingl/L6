package server.commands;

import server.util.Pair;

public class Add extends Command {

    public Add() {
        super("add", "добавить новый элемент в коллекцию");
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {
        return new Pair<>(false, "");
    }
}

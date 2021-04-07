package server.commands;

import shared.serializable.Pair;

public class Add extends Command {

    public Add() {
        super("add", "добавить новый элемент в коллекцию", true);
    }

    @Override
    public Pair<Boolean, String> execute(String arg, Object obj) {
        return new Pair<>(false, "");
    }
}

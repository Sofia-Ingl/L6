package server.commands;

import shared.serializable.Pair;

public interface Executables {
    Pair<Boolean, String> execute(String arg, Object obj);
}

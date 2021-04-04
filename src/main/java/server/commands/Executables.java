package server.commands;

import server.util.Pair;

public interface Executables {
    Pair<Boolean, String> execute(String arg, Object obj);
}

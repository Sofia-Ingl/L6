package client.util;

import shared.data.Movie;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;

import java.util.HashMap;
import java.util.Scanner;

public class Interaction {

    private Scanner scanner = new Scanner(System.in);
    private UserElementGetter userElementGetter;
    private HashMap<String, Pair<String, Boolean>> commandsAvailable;
    private boolean isScript = false;


    public Interaction(UserElementGetter userElementGetter) {
        //this.scanner = scanner;
        this.userElementGetter = userElementGetter;

        this.userElementGetter.setIn(System.in);
        this.userElementGetter.setOut(System.out);
        this.userElementGetter.setScanner(scanner);
    }

    public ClientRequest formRequest() {
        if (!isScript) {

            String[] commandWithArg;
            Movie movie = null;
            String command = "";
            String commandArg = "";
            boolean validation = false;

            while (!validation) {
                System.out.print(">");
                commandWithArg = (scanner.nextLine() + " ").split(" ", 2);
                command = commandWithArg[0].trim();
                commandArg = commandWithArg[1].trim();
                validation = commandIsValid(command);

                System.out.println(validation);
                System.out.println(commandsAvailable.get(command));
            }

            if (commandsAvailable.get(command).getSecond()) {
                movie = userElementGetter.movieGetter();
            }

            return new ClientRequest(command, commandArg, movie);
        }
        return null;
    }

    private boolean commandIsValid(String command) {
        return (commandsAvailable.get(command) != null);
    }


    public void setCommandsAvailable(HashMap<String, Pair<String, Boolean>> commandsAvailable) {
        this.commandsAvailable = commandsAvailable;
    }

    public HashMap<String, Pair<String, Boolean>> getCommandsAvailable() {
        return commandsAvailable;
    }
}

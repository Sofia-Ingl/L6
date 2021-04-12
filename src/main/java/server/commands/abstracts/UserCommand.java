package server.commands.abstracts;

public abstract class UserCommand extends Command {

    public UserCommand(String name, String utility, boolean isInteractive, boolean hasStringArg) {
        super(name, utility, isInteractive, hasStringArg);
    }

}

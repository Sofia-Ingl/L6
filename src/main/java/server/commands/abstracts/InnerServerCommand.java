package server.commands.abstracts;

public abstract class InnerServerCommand extends UserCommand {


    public InnerServerCommand(String name, String utility) {
        super(name, utility, false, false);
    }
}

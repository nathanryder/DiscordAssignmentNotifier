package ml.nathanryder.commands.command;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private Map<String, Command> commands = new HashMap<>();
    private Map<Command, Role> commandRoles = new HashMap<>();

    public void registerCommand(String cmd, Command listener, Role role, String... aliases) {
        for (String c : aliases)
            commands.put(c.toLowerCase(), listener);

        commands.put(cmd.toLowerCase(), listener);
        commandRoles.put(listener, role);
    }

    public void doCommand(String cmd, User sender, MessageChannel channel) {
        cmd = cmd.replace(cmd.split("")[0], "");
        String cmdName = cmd.split(" ")[0].toLowerCase();

        String[] args;
        if (cmd.length() == cmdName.length())
            args = new String[] {};
        else
            args = cmd.substring(cmdName.length() + 1, cmd.length()).split(" ");

        Command command = commands.get(cmdName);
        if (command == null)
            return;

        command.onCommand(cmdName, args, sender, channel);
    }

}

package ml.nathanryder.commands.command;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public interface Command {

    void onCommand(String cmd, String[] args, User sender, MessageChannel channel);

}

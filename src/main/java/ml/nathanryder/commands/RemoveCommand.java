package ml.nathanryder.commands;

import ml.nathanryder.Main;
import ml.nathanryder.Utils;
import ml.nathanryder.commands.command.Command;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class RemoveCommand implements Command {

    @Override
    public void onCommand(String cmd, String[] args, User sender, MessageChannel channel) {
        // .remove OOP 2019-02-02

        if (args.length < 2) {
            Utils.sendFancyMessage(channel, "Invalid arguments", "Correct usage: .remove (subject) (date)");
            return;
        }

        String subject = args[0];
        String date = args[1];

        boolean success = Main.getFiles().removeManualAssignment(subject + "_" + date);
        if (success)
            Utils.sendFancyMessage(channel, "Success", "Removed assignment");
        else
            Utils.sendFancyMessage(channel, "Error", "Failed to removed assignment");
    }

}

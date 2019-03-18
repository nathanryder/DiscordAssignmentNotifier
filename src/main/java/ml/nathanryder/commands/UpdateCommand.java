package ml.nathanryder.commands;

import ml.nathanryder.BlackBoardLogin;
import ml.nathanryder.Main;
import ml.nathanryder.Utils;
import ml.nathanryder.commands.command.Command;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class UpdateCommand implements Command {

    @Override
    public void onCommand(String cmd, String[] args, User sender, MessageChannel channel) {
        BlackBoardLogin blackboard = Main.getInstance().blackboardLogin(Main.USERNAME, Main.PASSWORD);
        blackboard.checkForAssignments();

        blackboard.sendAssignmentsToDiscord();
        Utils.sendFancyMessage(channel, "Update", "Update queued!");
    }

}

package ml.nathanryder.listeners;

import ml.nathanryder.Main;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageRecieveEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getName().equalsIgnoreCase("AssignmentBot"))
            return;

        MessageChannel channel =  event.getChannel();
        String msg = event.getMessage().getContentRaw();
        String[] msgdata = msg.split(" ");

        System.out.println("CMD: " + msgdata[0]);

        if (msg.split("")[0].equals(Main.PREFIX)) {
            Main.getCommands().doCommand(msg, event.getAuthor(), channel);
        }
    }

}

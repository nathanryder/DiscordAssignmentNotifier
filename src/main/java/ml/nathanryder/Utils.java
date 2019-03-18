package ml.nathanryder;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;

public class Utils {

    public static void sendFancyMessage(MessageChannel channel, String title, String message) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED);
        eb.setDescription(message);
        eb.setTitle(title);

        MessageEmbed built = eb.build();
        channel.sendMessage(built).queue();
    }

}

package ml.nathanryder.commands;

import ml.nathanryder.FileManager;
import ml.nathanryder.Main;
import ml.nathanryder.Utils;
import ml.nathanryder.commands.command.Command;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Iterator;

public class ListCommand implements Command {

    @Override
    public void onCommand(String cmd, String[] args, User sender, MessageChannel channel) {
        FileManager files = Main.getFiles();
        JSONObject config = null;
        try {
            config = files.getFileAsJSON("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (config == null) {
            channel.sendMessage("Failed to load config").queue();
            return;
        }
        JSONArray assignments = (JSONArray) config.get("assignments");
        if (assignments == null) {
            channel.sendMessage("Failed to load config!").queue();
            return;
        }

        StringBuilder message = new StringBuilder();
        for (Object o : assignments) {
            JSONObject assignment = (JSONObject) o;
            Iterator keys = assignment.keySet().iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();

                message.append(assignment.get(key)).append("\n");
            }
        }

        Utils.sendFancyMessage(channel, "Added Assignments", message.toString());
    }

}

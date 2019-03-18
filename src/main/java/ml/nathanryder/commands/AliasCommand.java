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

public class AliasCommand implements Command {

    @Override
    public void onCommand(String cmd, String[] args, User sender, MessageChannel channel) {

        if (args.length < 1) {
            Utils.sendFancyMessage(channel, "Invalid arguments", "For more information do " + Main.PREFIX + "alias help");
            return;
        }

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

        //Commands
        if (args[0].equalsIgnoreCase("help")) {
            String message = "**" + Main.PREFIX + "alias list** - Show all aliases\n";
            message += "**" + Main.PREFIX + "alias remove (alias)** - Remove an alias\n";
            message += "**" + Main.PREFIX + "alias (alias) (full name)** - Add a new alias";

            Utils.sendFancyMessage(channel, "Help Commands", message);
            return;
        } else if (args[0].equalsIgnoreCase("list")) {
            JSONArray aliases = (JSONArray) config.get("aliases");
            if (aliases == null) {
                channel.sendMessage("Failed to load config!").queue();
                return;
            }

            StringBuilder message = new StringBuilder();
            for (Object o : aliases) {
                JSONObject alias = (JSONObject) o;
                Iterator keys = alias.keySet().iterator();

                while (keys.hasNext()) {
                    String key = (String) keys.next();

                    message.append(key).append(" - ").append(alias.get(key)).append("\n");
                }
            }

            Utils.sendFancyMessage(channel, "Aliases", message.toString());
            return;
        } else if (args[0].equalsIgnoreCase("remove")) {

            if (args.length < 2) {
                channel.sendMessage("Invalid arguments! Correct usage: .alias remove (alias)").queue();
                return;
            }

            JSONArray aliases = (JSONArray) config.get("aliases");
            if (aliases == null) {
                channel.sendMessage("Failed to load config!").queue();
                return;
            }

            JSONArray newAliases = new JSONArray();
            for (Object o : aliases) {
                JSONObject alias = (JSONObject) o;
                Iterator keys = alias.keySet().iterator();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.equalsIgnoreCase(args[1]))
                        continue;

                    newAliases.add(alias);
                }
            }

            config.put("aliases", newAliases);

            try {
                files.writeFile("config.json", config.toJSONString());
            } catch (IOException e) {
                channel.sendMessage("Failed to write config!").queue();
                e.printStackTrace();
                return;
            }

            Utils.sendFancyMessage(channel, "", "Successfully removed alias for " + args[1]);
            return;
        }

        if (args.length < 2) {
            channel.sendMessage("Invalid arguments! Correct usage: .alias (alias) (full name)").queue();
            return;
        }

        String alias = args[0];
        StringBuilder full = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            full.append(args[i]).append(" ");
        }

        JSONArray aliases = (JSONArray) config.get("aliases");
        if (aliases == null)
            aliases = new JSONArray();

        JSONObject aliasJson = new JSONObject();
        aliasJson.put(alias, full.toString());
        aliases.add(aliases.size(), aliasJson);

        config.put("aliases", aliases);

        try {
            files.writeFile("config.json", config.toJSONString());
        } catch (IOException e) {
            channel.sendMessage("Failed to save file").queue();
            e.printStackTrace();
            return;
        }

        Utils.sendFancyMessage(channel, "Added alias", "Added alias for " + alias);
    }

}

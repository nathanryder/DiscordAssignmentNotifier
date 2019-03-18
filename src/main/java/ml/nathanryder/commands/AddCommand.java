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

public class AddCommand implements Command {

    @Override
    public void onCommand(String cmd, String[] args, User sender, MessageChannel channel) {
        // .add OOP 2019-02-02 Assignment 05

        if (args.length < 3) {
            Utils.sendFancyMessage(channel, "Invalid arguments", "Correct usage: .add (subject) (date) (assignment)");
            return;
        }

        String subject = args[0];
        String date = args[1];
        StringBuilder assignment = new StringBuilder();

        for (int i = 2; i < args.length; i++)
            assignment.append(args[i]).append(" ");

        boolean success = addAssignment(subject, assignment.toString(), date);
        if (success)
            Utils.sendFancyMessage(channel, "Success", "Successfully added assignment");
        else
            Utils.sendFancyMessage(channel, "Error", "Failed to add assignment");
    }

    public boolean addAssignment(String subject, String assignment, String date) {
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
            return false;
        }

        JSONArray assignments = (JSONArray) config.get("assignments");
        if (assignments == null)
            assignments = new JSONArray();

        String line = subject + " - " + assignment + "- " + date;
        JSONObject json = new JSONObject();
        json.put(subject + "_" + date, line);
        assignments.add(assignments.size(), json);

        config.put("assignments", assignments);
        try {
            files.writeFile("config.json", config.toJSONString());
            System.out.println("F");
        } catch (IOException e) {
            System.out.println("ERR");
            e.printStackTrace();
            return false;
        }

        return true;
    }

}

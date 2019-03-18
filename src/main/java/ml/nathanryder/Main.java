package ml.nathanryder;

import com.gargoylesoftware.htmlunit.WebClient;
import lombok.Getter;
import ml.nathanryder.commands.*;
import ml.nathanryder.commands.command.CommandManager;
import ml.nathanryder.commands.command.Role;
import ml.nathanryder.listeners.MessageRecieveEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class Main {

    private static String DISCORD_TOKEN = "";
    private static @Getter String ASSIGNMENT_CHANNEL = "";
    public static String PREFIX = ".";
    public static String USERNAME = "";
    public static String PASSWORD = "";
    private static @Getter JDA discord;

    public static @Getter CommandManager commands;
    public static @Getter FileManager files;
    public static @Getter Main instance;

    public Main() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        instance = this;
        commands = new CommandManager();
        files = new FileManager();

        //Load config
        try {
            files.createConfig();
            JSONObject array = files.getFileAsJSON("config.json");

            DISCORD_TOKEN = (String) array.get("discord_token");
            ASSIGNMENT_CHANNEL = (String) array.get("assignment_channel");
            USERNAME = (String) array.get("username");
            PASSWORD = (String) array.get("password");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        if (DISCORD_TOKEN.equals("CHANGEME")) {
            System.out.println("Config file not configured correctly: invalid discord_token");
            return;
        } else if (USERNAME.equals("CHANGEME")) {
            System.out.println("Config file not configured correctly: invalid username");
            return;
        } else if (PASSWORD.equals("CHANGEME")) {
            System.out.println("Config file not configured correctly: invalid password");
            return;
        }

        //Discord bot
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.addEventListener(new MessageRecieveEvent());
        builder.setToken(DISCORD_TOKEN);
        try {
            discord = builder.buildBlocking();
        } catch (LoginException | InterruptedException e) {
            System.out.println("DISCORD FAILED TO START.");
            System.out.println("SHUTTING DOWN..");
            e.printStackTrace();
            return;
        }

        commands.registerCommand("alias", new AliasCommand(), Role.MOD);
        commands.registerCommand("add", new AddCommand(), Role.USER);
        commands.registerCommand("remove", new RemoveCommand(), Role.USER);
        commands.registerCommand("list", new ListCommand(), Role.USER);
        commands.registerCommand("update", new UpdateCommand(), Role.MOD);

        Timer timer = new Timer();
        TimerTask t = new TimerTask () {
            @Override
            public void run () {
                BlackBoardLogin blackboard = blackboardLogin(USERNAME, PASSWORD);
                blackboard.checkForAssignments();

                blackboard.sendAssignmentsToDiscord();
//                blackboard.printAssigments();
            }
        };

        timer.schedule(t, 0L, 1000*60*60*12);
    }

    public BlackBoardLogin blackboardLogin(String username, String password) {
        WebClient webClient = new WebClient();
        BlackBoardLogin blackboard = new BlackBoardLogin(username, password);

        try {
            boolean login = blackboard.login();
            if (!login) {
                System.out.println("Invalid username or password for user " + username);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            webClient.close();
        }

        System.out.println("Successfully logged in");
        return blackboard;
    }

    public static void main(String[] args) {
        new Main();
    }


}

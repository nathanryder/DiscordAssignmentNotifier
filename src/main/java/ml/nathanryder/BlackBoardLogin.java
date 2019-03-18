package ml.nathanryder;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class BlackBoardLogin {

    private ArrayList<String> courseNames = new ArrayList<>();
    private List<Assignment> assignments = new ArrayList<>();


    private HtmlPage page;
    private WebClient webClient;
    private String username;
    private String password;

    public BlackBoardLogin(String username, String password) {
        this.username = username;
        this.password = password;
        webClient = new WebClient();
    }

    public boolean login() throws Exception {
        page = webClient.getPage("https://nuigalway.blackboard.com/webapps/login/");
        Thread.sleep(5000);

        HtmlForm form = page.getFormByName("login");
        HtmlInput username = form.getInputByName("user_id");
        username.setValueAttribute(this.username);

        HtmlInput password = form.getInputByName("password");
        password.setValueAttribute(this.password);

        page = page.getElementById("entry-login").click();

        DomElement error = page.getElementById("loginErrorMessage");

        if (error != null) {
            webClient.close();
            return false;
        }

        return true;
    }

    public void checkForAssignments() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assignments.clear();
        List<String> hiddenIds = getAllHiddenIds();

        for (String id : hiddenIds) {

            //Go to grades page
            String link = "https://nuigalway.blackboard.com/webapps/bb-mygrades-BBLEARN/myGrades?course_id="
                        + id + "1&stream_name=mygrades&is_stream=false";
            try {
                page = webClient.getPage(link);
            } catch (IOException e) {
                System.out.println("Failed to find grades page!");
                e.printStackTrace();
            }

            //Get course name
            String courseName = page.getElementById("courseMenu_link").getTextContent();
            courseNames.add(cleanupCourseName(courseName));

            //Get assignment name an due date
            for (Element element : page.getElementsByTagName("div")) {
                if (element.getAttribute("class").equals("cell gradable")) {

                    Assignment assignment = new Assignment();
                    String[] data = element.getTextContent().split("\n");

                    if (data.length < 4)
                        continue;

                    String due = "";
                    NodeList divs = element.getElementsByTagName("div");
                    for (int i = 0 ; i < divs.getLength(); i++) {

                        Element e2 = (Element) divs.item(i);
                        if (i == 0)
                            due = cleanDueDate(e2.getTextContent());
                    }

                    if (due == null)
                        continue;

                    assignment.setCourseName(cleanupCourseName(courseName));
                    assignment.setName(data[1]);
                    assignment.setDue(due);

                    assignments.add(assignment);
                }
            }


        }

        filterAssignments(assignments);
    }

    public void sendAssignmentsToDiscord() {
        TextChannel channel = Main.getDiscord().getTextChannelById(Main.getASSIGNMENT_CHANNEL());

        List<String> lines = new ArrayList<>();
        for (String courseName : courseNames) {

            for (Assignment assignment : assignments) {
                if (!assignment.getCourseName().equals(courseName))
                    continue;

                String msg = courseName + " - " + assignment.getName() + " - " + assignment.getDue();
                lines.add(msg);
            }
        }
        lines.addAll(Main.getFiles().getManualAssignments());


        List<LocalDate> dates = new ArrayList<>();
        for (String line : lines) {
            String[] data = line.split(" - ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(data[2].replace("*", ""), formatter);

            dates.add(date);
        }
        Collections.sort(dates);

        //Built message
        List<String> done = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < dates.size(); i++) {
            for (String line : lines) {
                String[] data = line.split(" - ");
                if (!data[2].equals(dates.get(i).toString()))
                    continue;
                if (done.contains(dates.get(i) + "_" + data[0])) {
                    System.out.println(dates.get(i) + "_" + data[0] + " ALREADY RAN");
                    continue;
                }

                done.add(dates.get(i) + "_" + data[0]);
                message.append("**").append(data[0]).append("** - ").append(data[1]).append(" - ");
                message.append("**").append(formatDate(data[2])).append("**\n");
            }
        }

        String msg = applyAliases(message.toString());

        //Check if there is already a message in discord
        Message l = null;
        try {
            l = channel.getMessageById(channel.getLatestMessageId()).complete();
        } catch (ErrorResponseException e) {
            System.out.println("No last message");
        }

        //Check if we need to update the message for new assignments
        boolean update = false;
        if (l != null) {
            String[] old = l.getContentDisplay().split("\n");

            if (old.length == lines.size()) {
                String[] newMsg = message.toString().split("\n");
                for (int i = 0; i < old.length; i++) {
                    String test = applyAliases(newMsg[i]);

                    if (!old[i].equals(test)) {
                        update = true;
                        break;
                    }
                }
            } else {
                update = true;
            }
        } else {
            update = true;
        }

        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = dateFormat.format(date);

        System.out.println("\nChecking for update at " +formattedDate);
        System.out.println("Update? " + update + "\n");

        if (!update)
            return;

        channel.getHistory().retrievePast(50).complete().forEach(e -> e.delete().queue());
        channel.sendMessage(msg).queue();

        //Nicer looking message - less readable
//        Calendar cal = Calendar.getInstance();
//        Date date = cal.getTime();
//        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
//        String formattedDate = dateFormat.format(date);
//
//        EmbedBuilder eb = new EmbedBuilder();
//        eb.setColor(Color.RED);
//        eb.setDescription(message);
//        eb.setTitle("Due Assignments");
//        eb.setFooter("Last updated: " + formattedDate, null);
//
//        MessageEmbed built = eb.build();
//        channel.sendMessage(built).queue();
    }

        public void printAssigments() {
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ASSIGNMENTS ");

        for (String courseName : courseNames) {
            System.out.println(" ");
            System.out.println(" ");
            System.out.println(" " + courseName);
            for (Assignment assignment : assignments) {
                if (!assignment.getCourseName().equals(courseName))
                    continue;

                System.out.println("    " + assignment.getName() + " due on " + assignment.getDue());
            }
        }
    }

    public String applyAliases(String message) {
        FileManager files = Main.getFiles();
        JSONObject config = null;
        try {
            config = files.getFileAsJSON("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String msg = message;
        JSONArray aliases = (JSONArray) config.get("aliases");
        if (aliases != null) {

            for (Object o : aliases) {
                JSONObject alias = (JSONObject) o;
                Iterator keys = alias.keySet().iterator();

                while (keys.hasNext()) {
                    String key = (String) keys.next();

                    msg = msg.replace(String.valueOf(alias.get(key)), key);
                }
            }
        }

        return msg;
    }

    public void filterAssignments(List<Assignment> assignments) {
        List<Assignment> filteredAssignments = new ArrayList<>();

        for (Assignment assignment : assignments) {
            if (!assignment.isDueBeforeToday())
                filteredAssignments.add(assignment);
        }

        this.assignments = filteredAssignments;
    }

    public List<String> getAllHiddenIds() {
        List<String> hiddenIds = new ArrayList<>();

        for (Element element : page.getElementsByTagName("a")) {
            String elementLink = element.getAttribute("href").replace(" ", "");
            Map<String, String> linkArgs = getLinkArguments(elementLink);

            if (linkArgs == null)
                continue;
            if (!linkArgs.containsKey("type"))
                continue;
            if (!linkArgs.get("type").equalsIgnoreCase("Course"))
                continue;



            String hiddenModuleID = linkArgs.get("id");
            hiddenIds.add(hiddenModuleID);
        }

        return hiddenIds;
    }

    public Map<String, String> getLinkArguments(String link) {
        String[] args = link.split("\\?");
        if (args.length <= 1)
            return null;

        Map<String, String> data = new HashMap<>();
        for (String line : args[1].split("&")) {
            String[] split = line.split("=");
            if (split.length <= 1)
                continue;

            String key = split[0];
            String value = split[1];
            data.put(key, value);
        }

        return data;
    }

    public String formatDate(String dateStr) {
        //Monday 10th Mar
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(dateStr, formatter);

        String prefix = "th";
        String month = String.valueOf(date.getDayOfMonth());
        if (month.length() == 1)
            month = "0" + month;
        System.out.println("Month: " + month);

        int first = Integer.parseInt(month.substring(0, 1));
        int digit = Integer.parseInt(month.substring(1, 2));

        if (digit == 1 && first != 1)
            prefix = "st";
        else if (digit == 2)
            prefix = "nd";
        else if (digit == 3)
            prefix = "rd";

        String newDate = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.UK);
        newDate += " " + date.getDayOfMonth() + prefix;
        newDate += " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.UK);

        return newDate;
    }

    public String cleanupCourseName(String courseName) {
        String[] data = courseName.replace(":", "").split(" ");

        StringBuilder name = new StringBuilder();
        for (int i = 1; i < data.length; i++) {
            name.append(data[i]).append(" ");
        }

        name.delete(name.length(), name.length());
        return name.toString();
    }

    public String cleanDueDate(String due) {
        String data[] = due.split(":");
        if (data.length == 1)
            return null;

        return data[1].replace(" ", "");
    }

    public String getPageText() {
        return page.getPage().asText();
    }

}

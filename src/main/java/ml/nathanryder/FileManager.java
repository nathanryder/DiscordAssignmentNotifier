package ml.nathanryder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class FileManager {

    public void createConfig() throws IOException {
        JSONObject json = new JSONObject();


        json.put("username", "CHANGEME");
        json.put("password", "CHANGEME");
        json.put("discord_token", "CHANGEME");
        json.put("assignment_channel", "CHANGEME");

        createFile("config.json", json.toJSONString());
    }

    public void createFile(String filename, String content) throws IOException {
        File dir = new File("config");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        File file = new File("config" + File.separator + filename);
        if (file.exists())
            return;

        writeFile(filename, content);
    }

    public void writeFile(String filename, String jsonString) throws IOException {
        FileWriter fileWriter = new FileWriter("config" + File.separator + filename);
        fileWriter.write(jsonString);
        fileWriter.close();
    }

    public JSONObject getFileAsJSON(String filename) throws IOException, ParseException {
        File file = new File("config" + File.separator + filename);
        if (!file.exists())
            return null;

        JSONParser parser = new JSONParser();
        FileReader fileReader = new FileReader("config" + File.separator + filename);

        JSONObject array = (JSONObject) parser.parse(fileReader);
        return array;
    }

    public ArrayList<String> getManualAssignments() {
        JSONObject config = null;
        try {
            config = getFileAsJSON("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (config == null)
            return null;

        JSONArray assignments = (JSONArray) config.get("assignments");
        if (assignments == null)
            return null;

        ArrayList<String> lines = new ArrayList<>();
        for (Object o : assignments) {
            JSONObject assignment = (JSONObject) o;
            Iterator keys = assignment.keySet().iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                LocalDate now = LocalDate.now();
                String dateStr = key.split("_")[1];

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
                LocalDate date = LocalDate.parse(dateStr, formatter);

                if (date.isBefore(now)) {
                    Main.getFiles().removeManualAssignment(key);
                    continue;
                }

                lines.add((String)assignment.get(key));
            }
        }

        return lines;
    }

    public boolean removeManualAssignment(String removeKey) {
        FileManager files = Main.getFiles();
        JSONObject config = null;
        try {
            config = files.getFileAsJSON("config.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray assignments = (JSONArray) config.get("assignments");
        if (assignments == null) {
            return false;
        }

        JSONArray newAssignments = new JSONArray();
        for (Object o : assignments) {
            JSONObject assignment = (JSONObject) o;
            Iterator keys = assignment.keySet().iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (key.equalsIgnoreCase(removeKey))
                    continue;

                newAssignments.add(assignment);
            }
        }

        config.put("assignments", newAssignments);
        try {
            files.writeFile("config.json", config.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}

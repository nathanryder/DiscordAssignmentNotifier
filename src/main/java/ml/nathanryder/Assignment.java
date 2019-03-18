package ml.nathanryder;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Assignment {

    private @Getter @Setter String courseName;
    private @Getter String name;
    private LocalDate due;

    public Assignment() {

    }

    public void setName(String name) {
        StringBuilder clean = new StringBuilder();

        boolean foundFirstLetter = false;
        char[] data = name.toCharArray();
        for (int i = 0; i < data.length; i++) {
            if (data[i] != ' ')
                foundFirstLetter = true;

            if (foundFirstLetter)
                clean.append(data[i]);
        }

        this.name = clean.toString();
    }

    public void setDue(String due) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(due, formatter);

        this.due = date;
    }

    public LocalDate getDue() {
        return due;
    }

    public boolean isDueBeforeToday() {
        LocalDate now = LocalDate.now();

        return due.isBefore(now);
    }

}

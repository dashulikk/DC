package model;

import java.util.List;

public class TermPaperPublication extends Publication {
    private int yearOfStudy;
    private String mentor;

    public TermPaperPublication(String title, String author, String text, List<String> links, int yearOfStudy, String mentor) {
        super(title, author, text, links);
        this.yearOfStudy = yearOfStudy;
        this.mentor = mentor;
    }

    // Getter and setter for yearOfStudy
    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    // Getter and setter for mentor
    public String getMentor() {
        return mentor;
    }

    public void setMentor(String mentor) {
        this.mentor = mentor;
    }
}


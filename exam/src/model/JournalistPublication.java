package model;

import java.util.List;

public class JournalistPublication extends Publication {
    private String paperName;
    private String journalisticScope;

    public JournalistPublication(String title, String author, String text, List<String> links, String paperName, String journalisticScope) {
        super(title, author, text, links);
        this.paperName = paperName;
        this.journalisticScope = journalisticScope;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getJournalisticScope() {
        return journalisticScope;
    }

    public void setJournalisticScope(String journalisticScope) {
        this.journalisticScope = journalisticScope;
    }
}


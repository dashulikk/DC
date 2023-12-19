package model;

import java.util.List;

public class SciencePublication extends Publication {
    private List<String> companions;
    private String scientificScope;

    public SciencePublication(String title, String author, String text, List<String> links, List<String> companions, String scientificScope) {
        super(title, author, text, links);
        this.companions = companions;
        this.scientificScope = scientificScope;
    }

    public List<String> getCompanions() {
        return companions;
    }

    public void setCompanions(List<String> companions) {
        this.companions = companions;
    }

    public String getScientificScope() {
        return scientificScope;
    }

    public void setScientificScope(String scientificScope) {
        this.scientificScope = scientificScope;
    }
}


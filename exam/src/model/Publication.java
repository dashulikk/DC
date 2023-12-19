package model;

import java.util.List;

public class Publication {
    private String title;
    private String author;
    private String text;
    private List<String> links;

    public Publication(String title, String author, String text, List<String> links) {
        this.title = title;
        this.author = author;
        this.text = text;
        this.links = links;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }
}


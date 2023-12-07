package ua.lab8.model;

import java.io.Serializable;

public class Folder implements Serializable {
    public String name;         // Назва папки

    public Folder(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Folder{ name=\"%s\" }", name);
    }
}

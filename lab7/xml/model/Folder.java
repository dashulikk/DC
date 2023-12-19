package ua.lab7.xml.model;

public class Folder {
    public int code;            // Унікальний код папки
    public String name;         // Назва папки

    public Folder(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Folder{ id=%d, name=\"%s\" }", code, name);
    }
}

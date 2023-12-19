package ua.lab7.xml.model;

import java.time.LocalDateTime;

public class File {
    public int code;                    // Унікальний код файлу
    public int folderCode;              // Папка в якій знаходиться файл
    public String name;                 // Ім'я файлу з розширенням
    public boolean isVisible;           //
    public boolean isReadable;          // Параметри файлу
    public boolean isWritable;          //
    public int size;                    // Розмір файлу в байтах
    public LocalDateTime lastUpdated;   // Дата та час останнього оновлення файлу

    public File(int code, int folderCode, String name, boolean isVisible, boolean isReadable, boolean isWritable, int size) {
        this(code, folderCode, name, isVisible, isReadable, isWritable, size, LocalDateTime.now());
    }

    public File(int code, int folderCode, String name, boolean isVisible, boolean isReadable, boolean isWritable, int size, LocalDateTime lastUpdated) {
        this.code = code;
        this.folderCode = folderCode;
        this.name = name;
        this.isVisible = isVisible;
        this.isReadable = isReadable;
        this.isWritable = isWritable;
        this.size = size;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return String.format("File{ id=%d, folderCode=%d, name=\"%s\", size=%d,\nvisible=%b, readable=%b, writeable=%b, lastUpdated=%s }",
                code, folderCode, name, size, isVisible, isReadable, isWritable, lastUpdated.toString());
    }
}

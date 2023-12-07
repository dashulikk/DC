package ua.lab8.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class File implements Serializable {
    public String folderName;           // Ім'я папки, у якій знаходиться файл
    public String fileName;                 // Ім'я файлу з розширенням
    public long size;                    // Розмір файлу в байтах
    public boolean isVisible;           //
    public boolean isReadable;          // Параметри файлу
    public boolean isWritable;          //
    public LocalDateTime lastUpdated;   // Дата та час останнього оновлення файлу

    public File(String folderName, String fileName, long size, boolean isVisible, boolean isReadable, boolean isWritable) {
        this(folderName, fileName, size, isVisible, isReadable, isWritable, LocalDateTime.now());
    }

    public File(String folderName, String fileName, long size, boolean isVisible, boolean isReadable, boolean isWritable, LocalDateTime lastUpdated) {
        this.folderName = folderName;
        this.fileName = fileName;
        this.size = size;
        this.isVisible = isVisible;
        this.isReadable = isReadable;
        this.isWritable = isWritable;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return String.format("File{ name=\"%s/%s\", size=%d, visible=%b, readable=%b, writeable=%b, lastUpdated=%s }",
                folderName, fileName, size, isVisible, isReadable, isWritable, lastUpdated.toString());
    }
}

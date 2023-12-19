package ua.lab7.xml.controller;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Scanner;

public interface Dao {
//    File getFile(int code);

    void createFile();

    void createFolder();

    void allFolders();

    void updateFile(int code);

    void updateFolder(int code);

    void deleteFile(int code);

    void deleteFolder(int code);

    void allFiles();

    void setScanner(Scanner scanner);

    void readFolder(int code);

    void readFolder(String name);

    void readFile(int code);

    void readFile(String name);

    void showDirectory(String name);

    void showDirectory(int code);

    void saveToFile(String fileName) throws IOException;

    void readFromFile(String fileName) throws IOException, SAXException;
}

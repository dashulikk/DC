package ua.lab7.xml.view;

import org.xml.sax.SAXException;
import ua.lab7.xml.controller.Dao;
import ua.lab7.xml.controller.DiskDao;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Viewer {
    private final Dao dao;
    private final String fileName = "/Users/dashulik/Library/Mobile Documents/com~apple~CloudDocs/KNU/5th semester/Distribued Computing/lab7/xml/disk.xml";

    public Viewer() {
        dao = new DiskDao();
        try {
            dao.readFromFile(fileName);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void begin() {
        Scanner scanner = new Scanner(System.in);
        dao.setScanner(scanner);
        showCommands();
        while (true) {
            System.out.print("Enter a command: ");
            String command = scanner.nextLine();
            if (command.equals("exit")) {
                try {
                    dao.saveToFile(fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            else if (command.equals("create file")) dao.createFile();
            else if (command.equals("create folder")) dao.createFolder();
            else if (command.startsWith("read folder ")) {
                String str = command.substring(12).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NoSuchElementException();
                    dao.readFolder(code);
                } catch (NumberFormatException e) {
                    dao.readFolder(str);
                }
            }
            else if (command.startsWith("read file ")) {
                String str = command.substring(10).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NoSuchElementException();
                    dao.readFile(code);
                } catch (NumberFormatException e) {
                    dao.readFile(str);
                }
            }
            else if (command.startsWith("dir ")) {
                String str = command.substring(4).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NoSuchElementException();
                    dao.showDirectory(code);
                } catch (NumberFormatException e) {
                    dao.showDirectory(str);
                }
            }
            else if (command.equals("folders")) dao.allFolders();
            else if (command.equals("files")) dao.allFiles();
            else if (command.startsWith("delete file ")) {
                String str = command.substring(12).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NumberFormatException();
                    dao.deleteFile(code);
                } catch (NumberFormatException e) {
                    System.out.println("After a \"delete file\" you must specify a non-negative integer value");
                }
            }
            else if (command.startsWith("delete folder ")) {
                String str = command.substring(14).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NoSuchElementException();
                    dao.deleteFolder(code);
                } catch (NumberFormatException e) {
                    System.out.println("After a \"delete folder\" you must specify a non-negative integer value");
                }
            }
            else if (command.startsWith("update file ")) {
                String str = command.substring(12).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NoSuchElementException();
                    dao.updateFile(code);
                } catch (NumberFormatException e) {
                    System.out.println("After a \"update file\" you must specify a non-negative integer value");
                }
            }
            else if (command.startsWith("update folder ")) {
                String str = command.substring(14).trim();
                try {
                    int code = Integer.parseInt(str);
                    if (code < 0) throw new NoSuchElementException();
                    dao.updateFolder(code);
                } catch (NumberFormatException e) {
                    System.out.println("After a \"update folder\" you must specify a non-negative integer value");
                }
            }
            else {
                System.out.printf("Command \"%s\" is not recognized\nTry one of the following commands:\n", command);
                showCommands();
            }
        }
    }

    public void showCommands() {
        System.out.println("""
                 - exit: save and exit
                 - create file: process instructions to create a new file
                 - create folder: process instructions to create a new folder
                 - read folder [code/name]: looking for a folder by code or name and shows it
                 - read file [code/name]: looking for a file by code or name and shows it
                 - dir [folderCode/folderName]: shows all files in the folder specified
                 - folders: shows all folders
                 - files: shows all files on the disk
                 - delete file [code]: deletes file with code [code]
                 - delete folder [code]: deletes folder with code [code]
                 - update file [code]: choose and change the attribute of the file with code [code]
                 - update folder [code]: choose and change the attribute of the folder with code [code]
                """);
    }
}

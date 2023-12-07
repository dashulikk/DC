package ua.lab8.client;

import java.util.Scanner;

public class View {

    private final Controller controller;
    private Scanner scanner;

    public View(Controller controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("All commands");
        showCommands();
        while (true) {
            System.out.print("Enter a command: ");
            scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            boolean exit = false;
            switch (command) {
                case "create folder" -> controller.createFolder();
                case "create file" -> controller.createFile();
                case "delete folder" -> controller.deleteFolder();
                case "delete file" -> controller.deleteFile();
                case "update file" -> controller.updateFile();
                case "move file" -> controller.moveFile();
                case "copy file" -> controller.copyFile();
                case "folders" -> controller.showAllFolders();
                case "files" -> controller.showAllFilesInFolder(true);
                case "exit" -> {
                    controller.exit();
                    exit = true;
                }
                default -> {
                    System.out.printf("Command \"%s\" is not recognized. List of all commands:\n", command);
                    showCommands();
                }
            }
            if (exit) break;
        }
    }

    private void showCommands() {
        System.out.println("""
                 - create folder: creates new folder with unique name
                 - create file: creates new file in the specified folder with unique name and provided attributes
                 - delete folder: deletes folder by specifying its name
                 - delete file: deletes file by specifying folder's name and file's name
                 - update file: updates specified parameter of the file
                 - move file: moves file to the specified folder
                 - copy file: copies file to the specified folder
                 - folders: shows list of all folders
                 - files: shows all files of specified folder
                 - exit: finish program's execution
                """);
    }
}

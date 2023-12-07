package ua.lab8.server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ua.lab8.model.File;
import ua.lab8.model.Folder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;

public class SocketServer {

    public static final String HOST = "localhost";
    public static final int PORT = 10352;    private final BufferedReader in;
    private final PrintWriter out;
    private final ServerSocket server;
    private final DAO dao;
    private final Socket client;

    public SocketServer(DAO dao) {
        this.dao = dao;
        try {
            server = new ServerSocket(PORT);
            System.out.println("Waiting for client...");
            client = server.accept();
            System.out.println("Client is connected");
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            begin();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void begin() {
        while (true) {
            String command = readLine();
            switch (command) {
                case "create folder" -> createFolder();
                case "create file" -> createFile();
                case "delete folder" -> deleteFolder();
                case "delete file" -> deleteFile();
                case "update file" -> updateFile();
                case "copy file" -> copyFile();
                case "folders" -> readAllFolders();
                case "files" -> readAllFilesInFolder();
                case "exit" -> {
                    close();
                    System.exit(0);
                }
                default ->
                        throw new IllegalArgumentException("Command \"" + command + "\" is not recognized");
            }
        }
    }

    private void createFolder() {
        Folder folder;
        folder = new Folder(readLine());
        String response = dao.saveIfAbsent(folder) ? "success" :
                "Folder \"" + folder.name + "\" is already present in file system";
        out.println(response);
    }

    private void createFile() {
        File file;
        StringBuilder sb = new StringBuilder();
        while (true) {
            String str = readLine();
            if (str.equals("end")) break;
            sb.append(str).append('\n');
        }
        JSONObject jo;
        try {
            jo = (JSONObject) new JSONParser().parse(sb.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        file = new File(
                (String) jo.get("folder_name"),
                (String) jo.get("file_name"),
                Long.parseLong((String) jo.get("size")),
                Boolean.parseBoolean((String) jo.get("is_visible")),
                Boolean.parseBoolean((String) jo.get("is_readable")),
                Boolean.parseBoolean((String) jo.get("is_writeable")),
                Timestamp.valueOf((String) jo.get("last_updated")).toLocalDateTime());
        String response = dao.saveIfAbsent(file) ? "success" :
                "File with name \"" + file.fileName + "\" is already present in folder \"" + file.folderName + "\"";
        out.println(response);
    }

    private void deleteFolder() {
        String folderName = readLine();
        dao.deleteFiles(folderName);
        out.println(dao.deleteFolder(folderName));
    }

    private void deleteFile() {
        var path = readLine().split("/");
        out.println(dao.deleteFile(path[0], path[1]));
    }

    private void updateFile() {
        String[] pathAndUpdate = readLine().split("\\*");
        String[] path = pathAndUpdate[0].split("/");
        String[] update = pathAndUpdate[1].split(":");
        dao.updateFile(path[0], path[1], update[0], update[1]);
    }

    private void copyFile() {
        String[] srcFileDst = readLine().split("/");
        dao.copyFile(srcFileDst[0], srcFileDst[1], srcFileDst[2]);
    }

    private void readAllFolders() {
        out.println(dao.readAllFolders());
    }

    private void readAllFilesInFolder() {
        out.println(dao.readAllFilesInFolder(new Folder(readLine())));
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            client.close();
            server.close();
            in.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

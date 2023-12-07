package ua.lab8.client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ua.lab8.model.File;
import ua.lab8.model.Folder;
import ua.lab8.server.RMIServer;
import ua.lab8.server.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Stream;

public class RMIClient implements Client {

    private final Server server;

    public RMIClient() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIServer.PORT);
            server = (Server) registry.lookup(RMIServer.UNIQUE_BINDING_NAME);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> queryFolders() {
        try {
            return Stream.of(server.readAllFolders().split("\n")).filter(name -> !name.equals("end")).toList();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String saveFile(String jsonObj) {
        JSONParser parser = new JSONParser();
        JSONObject jo;
        try {
            jo = (JSONObject) parser.parse(jsonObj.substring(0, jsonObj.length() - 4));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        File file = new File(
                (String) jo.get("folder_name"),
                (String) jo.get("file_name"),
                Long.parseLong((String) jo.get("size")),
                Boolean.parseBoolean((String) jo.get("is_visible")),
                Boolean.parseBoolean((String) jo.get("is_readable")),
                Boolean.parseBoolean((String) jo.get("is_writeable")),
                Timestamp.valueOf((String) jo.get("last_updated")).toLocalDateTime());
        try {
            return server.createFile(file);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String saveFolder(String folderName) {
        try {
            return server.createFolder(new Folder(folderName));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String queryDeleteFolder(String folderName) {
        try {
            return server.deleteFolder(folderName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String queryDeleteFile(String folderName, String fileName) {
        try {
            return server.deleteFile(folderName, fileName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void queryUpdateFile(String updateExpression) {
        String[] pathAndUpdate = updateExpression.split("\\*");
        String[] path = pathAndUpdate[0].split("/");
        String[] update = pathAndUpdate[1].split(":");
        try {
            server.updateFile(path[0], path[1], update[0], update[1]);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void queryCopyFile(String copyExpression) {
        String[] srcFileDst = copyExpression.split("/");
        try {
            server.copyFile(srcFileDst[0], srcFileDst[1], srcFileDst[2]);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> queryFiles(String folderName) {
        try {
            return Stream.of(server.readAllFilesInFolder(folderName).split("\n")).filter(name -> !name.equals("end")).toList();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            server.close();
        } catch (RemoteException e) {
            System.exit(0);
        }
    }
}

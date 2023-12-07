package ua.lab8.client;

import ua.lab8.server.SocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient implements Client {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public SocketClient() {
        try {
            socket = new Socket(SocketServer.HOST, SocketServer.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> queryFolders() {
        out.println("folders");
        List<String> folders = new ArrayList<>();
        try {
            while (true) {
                String str = in.readLine();         // something like 'Folder{ name="Documents" }'
                if (str.equals("end")) break;
                folders.add(str);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return folders;
    }

    @Override
    public String saveFile(String jsonObj) {
        out.println("create file");
        out.println(jsonObj);

        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String saveFolder(String folderName) {
        out.println("create folder");
        out.println(folderName);

        try {
            return in.readLine().replace('\\', '\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String queryDeleteFolder(String folderName) {
        out.println("delete folder");
        out.println(folderName);

        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String queryDeleteFile(String folderName, String fileName) {
        out.println("delete file");
        out.println(folderName + "/" + fileName);

        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void queryUpdateFile(String updateExpression) {
        out.println("update file");
        out.println(updateExpression);
    }

    @Override
    public void queryCopyFile(String copyExpression) {
        out.println("copy file");
        out.println(copyExpression);
    }

    @Override
    public List<String> queryFiles(String folderName) {
        out.println("files");
        out.println(folderName);
        List<String> files = new ArrayList<>();
        try {
            do {
                String str = in.readLine();
                if (str.equals("end"))
                    break;
                files.add(str);
            } while (true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    @Override
    public void close() {
        out.println("exit");
        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

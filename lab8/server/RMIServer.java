package ua.lab8.server;

import ua.lab8.model.File;
import ua.lab8.model.Folder;

import java.rmi.RemoteException;

public class RMIServer implements Server {

    public static final int PORT = 1099;
    public static final String UNIQUE_BINDING_NAME = "server.fileSystem";

    private final DAO dao;

    public RMIServer(DAO dao) {
        this.dao = dao;
    }

    @Override
    public String createFolder(Folder folder) throws RemoteException {
        return dao.saveIfAbsent(folder) ? "success" :
                "Folder \"" + folder.name + "\" is already present in file system";
    }

    @Override
    public String createFile(File file) throws RemoteException {
        return dao.saveIfAbsent(file) ? "success" :
                "File with name \"" + file.fileName + "\" is already present in folder \"" + file.folderName + "\"";
    }

    @Override
    public String deleteFolder(String folderName) throws RemoteException {
        dao.deleteFiles(folderName);
        return dao.deleteFolder(folderName);
    }

    @Override
    public String deleteFile(String folderName, String fileName) throws RemoteException {
        return dao.deleteFile(folderName, fileName);
    }

    @Override
    public void updateFile(String folderName, String fileName, String attr, String value) throws RemoteException {
        dao.updateFile(folderName, fileName, attr, value);
    }

    @Override
    public void copyFile(String srcFolderName, String fileName, String dstFolderName) throws RemoteException {
        dao.copyFile(srcFolderName, fileName, dstFolderName);
    }

    @Override
    public String readAllFolders() throws RemoteException {
        return dao.readAllFolders();
    }

    @Override
    public String readAllFilesInFolder(String folderName) throws RemoteException {
        return dao.readAllFilesInFolder(new Folder(folderName));
    }

    @Override
    public void close() throws RemoteException {
        System.exit(0);
    }
}

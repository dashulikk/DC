package ua.lab8.server;

import ua.lab8.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

    String createFolder(Folder folder) throws RemoteException;

    String createFile(File file) throws RemoteException;

    String deleteFolder(String folderName) throws RemoteException;

    String deleteFile(String folderName, String fileName) throws RemoteException;

    void updateFile(String folderName, String fileName, String attr, String value) throws RemoteException;

    void copyFile(String srcFolderName, String fileName, String dstFolderName) throws RemoteException;

    String readAllFolders() throws RemoteException;

    String readAllFilesInFolder(String folderName) throws RemoteException;

    void close() throws RemoteException;
}

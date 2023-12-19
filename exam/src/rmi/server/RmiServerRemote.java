package rmi.server;

import model.Publication;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RmiServerRemote extends Remote {

    List<Publication> findLinkedPublications(String title) throws RemoteException;

}

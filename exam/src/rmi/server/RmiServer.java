package rmi.server;

import common.Library;
import model.Publication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RmiServer extends UnicastRemoteObject implements RmiServerRemote {
    Library library;

    protected RmiServer() throws RemoteException {
        this.library = new Library(new ArrayList<>());
        this.library.fillLibrary();
    }

    @Override
    public List<Publication> findLinkedPublications(String title) throws RemoteException {
        Publication publication = library.getPublicationByTitle(title);
        List<String> titles = new ArrayList<>();
        if (publication != null) {
            titles = library.getLinkedPublications(publication);
        }
        List<Publication> publications = new ArrayList<>();
        for (String t : titles) {
            publications.add(library.getPublicationByTitle(t));
        }
        return publications;
    }
}

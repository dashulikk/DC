package rmi.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {

    public static void main(String[] args) {
        System.out.println("==> Server started");

        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(8080);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        RmiServerRemote service = null;
        try {
            service = new RmiServer();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            registry.rebind("server", service);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}

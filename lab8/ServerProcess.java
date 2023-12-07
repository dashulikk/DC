package ua.lab8;

import ua.lab8.server.AMQServer;
import ua.lab8.server.DAO;
import ua.lab8.server.RMIServer;
import ua.lab8.server.SocketServer;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerProcess {
    public static void main(String[] args) {
        DAO dao = new DAO();
        rmiServer(dao);
    }

    private static void socketServer(DAO dao) {
        new SocketServer(dao);
    }

    private static void rmiServer(DAO dao) {
        try {
            final RMIServer server = new RMIServer(dao);
            final Registry registry = LocateRegistry.createRegistry(RMIServer.PORT);
            Remote stub = UnicastRemoteObject.exportObject(server, 0);
            registry.bind(RMIServer.UNIQUE_BINDING_NAME, stub);

            Thread.sleep(Long.MAX_VALUE);
        } catch (RemoteException | AlreadyBoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void jmsServer(DAO dao) {
        var server = new AMQServer(dao);
        while (!server.isDidUserExit()) Thread.onSpinWait();
    }
}

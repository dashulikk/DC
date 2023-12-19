package rmi;


import rmi.server.ServerMain;
import rmi.client.ClientMain;

public class Main {

    public static void main(String[] args) {
        Thread serverThread = new Thread(
                () -> ServerMain.main(args)
        );

        Thread clientThread = new Thread(
                () -> ClientMain.main(args)
        );


        serverThread.start();
        clientThread.start();
    }

}

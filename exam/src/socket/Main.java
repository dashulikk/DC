package socket;

import socket.client.ClientMain;
import socket.server.ServerMain;

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

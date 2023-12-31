package socket.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServer {

    private static final Integer PORT = 8080;
    private static final AtomicBoolean isStopped = new AtomicBoolean(false);

    public void start() {
        System.out.println("==> Server started");

        try (ServerSocket server = new ServerSocket(PORT)) {
            server.setReuseAddress(true);

            while (!isStopped.get()) {
                Socket client = server.accept();
                System.out.println("==> New client connected: " + client.getInetAddress().getHostAddress());

                Runnable clientRunnable = () -> {
                    RequestProcessor requestProcessor = new RequestProcessor(this, client);
                    while(!isStopped.get()){
                        requestProcessor.process();
                    }
                };

                new Thread(clientRunnable).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("==> Server stopped");
    }

    public void stop() {
        isStopped.set(true);
    }

    public int getPort() {
        return PORT;
    }

}

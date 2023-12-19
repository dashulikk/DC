package socket.client;

import common.AttributeType;
import common.OperationType;
import common.Request;
import common.Response;
import model.Publication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SocketClient {

    private static final Integer PORT = 8080;

    public void start() {
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        boolean isStopped = false;

        try(
                Socket socket = new Socket(host, PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)
        ) {
            while(true){
                System.out.println(
                    "Choose option: \n" +
                            "1 - " + OperationType.LINKED_PUBLICATIONS + "\n" +
                            "2 - " + OperationType.TITLE_PUBLICATION + "\n" +
                            "3 - " + OperationType.AUTHOR_PUBLICATION + "\n" +
                            "4 - " + OperationType.CLOSE + "\n"
                );

                int commandNumber = scanner.nextInt();
                scanner.nextLine();

                Request request = null;
                Map<String, Object> attributes = new HashMap<>();
                switch (commandNumber) {
                    case 1: {
                        System.out.println("Enter title: ");
                        String title = scanner.nextLine();

                        attributes.put(AttributeType.TITLE.name(), title);

                        request = new Request(OperationType.LINKED_PUBLICATIONS, attributes);
                        break;
                    }
                    case 2: {
                        System.out.println("Enter title: ");
                        String title = scanner.nextLine();

                        attributes.put(AttributeType.TITLE.name(), title);

                        request = new Request(OperationType.TITLE_PUBLICATION, attributes);
                        break;
                    }
                    case 3: {
                        System.out.println("Enter author: ");
                        String author = scanner.nextLine();

                        attributes.put(AttributeType.AUTHOR.name(), author);

                        request = new Request(OperationType.AUTHOR_PUBLICATION, attributes);
                        break;
                    }
                    case 4: {
                        System.out.println("==> ClientMain stopped");
                        request = new Request(OperationType.CLOSE, attributes);
                        isStopped = true;
                        break;
                    }
                }

                out.writeObject(request);

                if(isStopped) break;

                System.out.println("Result: ");
                Response response = (Response) in.readObject();

                List<Publication> publications = response.getPublications();
                publications.forEach(System.out::println);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}

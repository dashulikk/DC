package socket.server;

import common.*;
import model.Publication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RequestProcessor {

    private final SocketServer server;
    private final Socket client;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Library library;


    public RequestProcessor(SocketServer server, Socket client) {
        this.server = server;
        this.client = client;
        this.library = new Library(new ArrayList<>());
        this.library.fillLibrary();
        try {
            this.in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.out = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void process() {
        Request request = receiveRequest();
        OperationType operationType = request.getOperationType();

        System.out.println(client.getInetAddress().getHostAddress() + ": " + operationType.name());

        Response response = null;
        Map<String, Object> attributes = request.getAttributes();

        switch (operationType) {
            case LINKED_PUBLICATIONS: {
                String title = (String) attributes.get(AttributeType.TITLE.name());
                Publication p = library.getPublicationByTitle(title);
                List<String> publicationTitles = new ArrayList<>();
                if (p != null) {
                    publicationTitles = library.getLinkedPublications(p);
                }
                List<Publication> publicationsFromTitles = new ArrayList<>();
                for (String t : publicationTitles) {
                    publicationsFromTitles.add(library.getPublicationByTitle(t));
                }
                response = new Response(publicationsFromTitles);
                break;
            }
            case TITLE_PUBLICATION: {
                String title = (String) attributes.get(AttributeType.TITLE.name());
                Publication p = library.getPublicationByTitle(title);
                response = new Response(Arrays.asList(p));
                break;
            }
            case AUTHOR_PUBLICATION: {
                String author = (String) attributes.get(AttributeType.AUTHOR.name());
                List<Publication> p = library.getPublicationsByAuthor(author);
                response = new Response(p);
                break;
            }
            case CLOSE: {
                server.stop();
                break;
            }
        }


        sendResponse(response);
    }

    private Request receiveRequest() {
        try {
            return (Request) in.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendResponse(Response response) {
        try {
            out.writeObject(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

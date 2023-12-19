package common;

import model.Publication;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {

    private final List<Publication> publications;

    public Response(List<Publication> publications) {
        this.publications = publications;
    }

    public List<Publication> getPublications() {
        return publications;
    }
}

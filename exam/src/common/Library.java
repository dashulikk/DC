package common;

import model.JournalistPublication;
import model.Publication;
import model.SciencePublication;
import model.TermPaperPublication;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Library {
    private List<Publication> publications;

    public Library(List<Publication> publications) {
        this.publications = publications;
    }

    public void fillLibrary() {
        addPublication(new JournalistPublication("Title1", "Author1", "Text1", Arrays.asList("Link1", "Link2"), "Paper1", "Scope1"));
        addPublication(new SciencePublication("Title2", "Author2", "Text2", Arrays.asList("Link3", "Link4"), Arrays.asList("Companion1"), "ScientificScope1"));
        addPublication(new TermPaperPublication("Title3", "Author3", "Text3", Arrays.asList("Link5", "Link6"), 2021, "Mentor1"));
    }

    public void addPublication(Publication publication) {
        if (publications.stream().anyMatch(pub -> pub.getTitle().equalsIgnoreCase(publication.getTitle()))) {
            return;
        }
        publications.add(publication);
    }

    public List<String> getLinkedPublications(Publication publication) {
        return publication.getLinks();
    }

    public Publication getPublicationByTitle(String title) {
        return publications.stream()
                .filter(publication -> publication.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    public List<Publication> getPublicationsByAuthor(String author) {
        return publications.stream()
                .filter(publication -> publication.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public List<Publication> getPublicationsByKeywords(List<String> keywords) {
        return publications.stream()
                .filter(publication -> keywords.stream()
                        .allMatch(keyword -> publication.getText().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))))
                .collect(Collectors.toList());
    }

    public List<Publication> sortPublicationsByInput(String input) {
        return publications.stream()
                .sorted((pub1, pub2) -> Integer.compare(Math.abs(pub1.getTitle().compareToIgnoreCase(input)),
                        Math.abs(pub2.getTitle().compareToIgnoreCase(input))))
                .collect(Collectors.toList());
    }
}


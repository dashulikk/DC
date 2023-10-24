package src;

import java.util.ArrayList;
import java.util.Random;

class Book {
    private final long id;
    private final boolean readerRoomOnly;

    public Book(long id, boolean readerRoomOnly) {
        this.id = id;
        this.readerRoomOnly = readerRoomOnly;
    }

    public long getId() {
        return id;
    }

    public boolean isReaderRoomOnly() {
        return readerRoomOnly;
    }
}

class ReadingRoom {
    private int readersInRoom = 0;

    public synchronized void enter(Reader reader) {
        readersInRoom++;
        reader.setLocation("reading room");
        System.out.println("Reader " + reader.getName() + " entered reading room");
        System.out.println("Readers in reading room: " + readersInRoom);
        notify();
    }

    public synchronized void leave(Reader reader) {
        readersInRoom--;
        reader.setLocation("building");
        System.out.println("Reader " + reader.getName() + " left reading room");
        System.out.println("Readers in reading room: " + readersInRoom);
        notify();
    }
}

class Library{
    private int readersInRoom;

    private ArrayList<Book> books;

    private ReadingRoom readingRoom;

    public Library(ReadingRoom readingRoom, ArrayList<Book> books) {
        this.readingRoom = readingRoom;
        this.books = books;
        this.readersInRoom = 0;
    }

    public synchronized void enter(Reader reader) {
        readersInRoom++;
        reader.setLocation("library");
        System.out.println("Reader " + reader.getName() + " entered library");
        System.out.println("Readers in library room: " + readersInRoom);
        notify();
    }

    public synchronized void leave(Reader reader) {
        readersInRoom--;
        reader.setLocation("near");
        System.out.println("Reader " + reader.getName() + " left library");
        System.out.println("Readers in library room: " + readersInRoom);
        notify();
    }


    public synchronized void giveBook(Reader reader, Book book) {
        ArrayList<Book> readerBooks = reader.getBooks();
        readerBooks.add(book);
        reader.setBooks(readerBooks);
        books.remove(book);
        System.out.println("Reader " + reader.getName() + " took the book "
                + book.getId());
    }

    public synchronized void returnBook(Reader reader, Book book) {
        ArrayList<Book> readerBooks = reader.getBooks();
        readerBooks.remove(book);
        reader.setBooks(readerBooks);
        books.add(book);
        System.out.println("Reader " + reader.getName() + " returned the book "
                + book.getId());
    }

    ArrayList<Book> getBooks() {
        return books;
    }

    public ReadingRoom getReadingRoom() {
        return readingRoom;
    }
}

class Reader implements Runnable {

    private String name;
    private String location;
    private Library library;
    private ArrayList<Book> books;

    Reader(Library library, String name) {
        this.library = library;
        this.name = name;
        location = "home";
        books = new ArrayList<>();
    }

    private int chooseAction() {
        int rnd = new Random().nextInt();
        if (location.equals("home")) {
            if (rnd % 2 == 0) return 6;
            return 8;
        } else if (location.equals("library")) {
            if (rnd % 3 == 0) return 1;
            if (rnd % 3 == 1) return 5;
            return 7;
        } else if (location.equals("near")) {
            if (rnd % 2 == 0) return 2;
            return 0;
        } else if (location.equals("reading room")) {
            if (rnd % 2 == 0) return 6;
            return 3;
        }
        return -1;
    }

    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                int action = chooseAction();
                switch (action) {
                    case 0:
                        library.enter(Reader.this);
                        Thread.sleep(1000);
                        break;
                    case 1:
                        library.leave(Reader.this);
                        Thread.sleep(1000);
                        break;
                    case 2:
                        library.getReadingRoom().enter(Reader.this);
                        Thread.sleep(2000);
                        break;
                    case 3:
                        library.getReadingRoom().leave(Reader.this);
                        Thread.sleep(2000);
                        break;
                    case 4:
                        setLocation("Home");
                        System.out.println("Reader " + this.name + " is at home");
                        Thread.sleep(3000);
                        break;
                    case 5:
                        if (!library.getBooks().isEmpty()) {
                            library.giveBook(Reader.this, library.getBooks().
                                    get(new Random().nextInt(library.
                                            getBooks().size())));
                            Thread.sleep(2000);
                            if(Reader.this.books.get(books.size() - 1).isReaderRoomOnly()) {
                                readAtReadingRoom(Reader.this.books.get(books.size() - 1));
                            }
                        }
                        break;
                    case 6:
                        if (!Reader.this.books.isEmpty()) {
                            System.out.println("Reader " + this.name + " is reading");
                            Thread.sleep(5000);
                        } else break;
                        break;
                    case 7:
                        if (!Reader.this.books.isEmpty()) {
                            library.returnBook(Reader.this, Reader.this.getBooks().
                                    get(new Random().nextInt(Reader.this.
                                            getBooks().size())));
                        } else break;
                        break;
                    case 8:
                        Reader.this.setLocation("near");
                        System.out.println("Reader " + Reader.this.name + " is near");
                        break;
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void readAtReadingRoom(Book book) {
        try {
            setLocation("reading room");
            System.out.println("Reader " + this.name + " is reading the book " + book.getId() + " which is allowed to "
                    + "read only in reading room");
            Thread.sleep(10000);
            setLocation("library");
            library.returnBook(Reader.this, book);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    String getName() {
        return name;
    }


    ArrayList<Book> getBooks() {
        return books;
    }

    void setBooks(ArrayList<Book> books) {
        this.books = books;
    }

    void setLocation(String location) {
        this.location = location;
    }
}

public class Task1Java {
    public static void main(String[] args) {
        ArrayList<Book> books = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            Book book = new Book(i, new Random().nextBoolean());
            books.add(book);
        }

        Library library = new Library(new ReadingRoom(), books);

        Thread reader1 = new Thread(new Reader(library, "Ivan"), "reader1");
        Thread reader2 = new Thread(new Reader(library, "Daryna"), "reader2");
        Thread reader3 = new Thread(new Reader(library, "Danylo"), "reader3");
        Thread reader4 = new Thread(new Reader(library, "Tamara"), "reader4");

        reader1.start();
        reader2.start();
        reader3.start();
        reader4.start();
    }
}

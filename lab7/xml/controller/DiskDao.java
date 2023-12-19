package ua.lab7.xml.controller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import org.xml.sax.SAXParseException;
import ua.lab7.xml.model.File;
import ua.lab7.xml.model.Folder;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiskDao implements Dao {

    private final List<Folder> folders;
    private final List<File> files;
    private final DocumentBuilder documentBuilder;
    private final Transformer transformer;
    private Scanner scanner;

    public DiskDao() {
        folders = new ArrayList<>();
        files = new ArrayList<>();

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;

        try {
            schema = schemaFactory.newSchema(new java.io.File("src/ua/lab7/xml/schema.xsd"));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setSchema(schema);

        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        documentBuilder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) {
                System.err.println("Warning");
                e.printStackTrace();
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public boolean askUser(String question) {
        while (true) {
            scanner = new Scanner(System.in);
            System.out.printf("%s (y/n): ", question);
            String answer = scanner.nextLine();
            if (answer.equals("y"))
                return true;
            else if (answer.equals("n"))
                return false;
            System.out.printf("Don't understand \"%s\"\n", answer);
        }
    }

    @Override
    public void createFile(){
        if (folders.isEmpty()) {
            System.out.println("ERROR: before file creation at least one folder must exist");
            return;
        }
        int code = files.isEmpty() ? 0 : Collections.max(files.stream().map(file -> file.code).toList()) + 1;
        int folderCode;
        // Зчитування коду папку, де буде знаходитися файл та обробка помилок
        Optional<Folder> optionalFolder;
        while (true) {
            System.out.print("Folder code where to place the file: ");
            String line = scanner.nextLine();
            try {
                folderCode = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Folder code must be an integer");
                continue;
            }
            int finalFolderCode = folderCode;
            if ((optionalFolder = folders.stream().filter(folder -> folder.code == finalFolderCode).findFirst()).isPresent())
                break;
            System.out.printf("There is no folder with code %d\n", folderCode);
            if (!askUser("Try another code?"))
                return;
        }
        String name;
        int finalFolderCode = folderCode;
        String folderName = optionalFolder.get().name;
        // Зчитування імені файлу, та обробка ситуації, коли в одній папці два файли з однаковим іменем
        while (true) {
            System.out.print("File name: ");
            name = scanner.nextLine();
            // validate filename
            try {
                Path.of(name);
            } catch (InvalidPathException e) {
                System.out.println(e.getMessage());
                if (askUser("Create file with another name?"))
                    continue;
            }
            String finalName = name;
            if (files.stream().noneMatch(file -> file.folderCode == finalFolderCode && file.name.equals(finalName))) {
                break;
            } else {
                System.out.printf("File with name \"%s\" is already present in folder \"%s\"\n", name, folderName);
                if (!askUser("Create file with another name?"))
                    return;
            }
        }
        int size;
        while (true) {
            System.out.print("File size in bytes: ");
            try {
                size = Integer.parseInt(scanner.nextLine());
                if (size < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: size of file must be a positive integer");
            }
        }
        System.out.println("# File properties. Everything except \"true\" is considered as false");
        System.out.print("Is file visible: ");
        boolean visible = scanner.nextLine().equals("true");
        System.out.print("Is file readable: ");
        boolean readable = scanner.nextLine().equals("true");
        System.out.print("Is file writeable: ");
        boolean writeable = scanner.nextLine().equals("true");
        files.add(new File(code, folderCode, name, visible, readable, writeable, size));
        System.out.printf("File \"%s/%s\" with code %d is successfully created\n", folderName, name, code);
    }

    @Override
    public void createFolder() {
        while (true) {
            System.out.print("Folder name: ");
            String name = scanner.nextLine();
            try {
                Path.of(name + "/file.txt");
            } catch (InvalidPathException e) {
                System.out.println("Incorrect folder name");
                if (askUser("Do you want to choose another name?"))
                    continue;
                else
                    break;
            }
            if (folders.stream().anyMatch(folder -> folder.name.equals(name))) {
                System.out.printf("Name \"%s\" is already present on disk\n", name);
                if (!askUser("Do you want to choose another name?"))
                    break;
            } else {
                int code = folders.isEmpty() ? 0 : Collections.max(folders.stream().map(folder -> folder.code).toList()) + 1;
                folders.add(new Folder(code, name));
                System.out.printf("Folder \"%s\" with code %d is successfully created\n", name, code);
                break;
            }
        }
    }

    @Override
    public void readFile(int code) {
        Optional<File> optionalFile;
        if ((optionalFile = files.stream().filter(file -> file.code == code).findFirst()).isPresent())
            System.out.println(optionalFile.get());
        else
            System.out.println("There is no file with code " + code);
    }

    @Override
    public void readFile(String name) {
        Optional<File> optionalFile;
        if ((optionalFile = files.stream().filter(file -> file.name.equals(name)).findFirst()).isPresent())
            System.out.println(optionalFile.get());
        else
            System.out.printf("There is no file with name \"%s\" on disk\n", name);
    }

    @Override
    public void readFolder(int code) {
        Optional<Folder> optionalFolder;
        if ((optionalFolder = folders.stream().filter(folder -> folder.code == code).findFirst()).isPresent())
            System.out.println(optionalFolder.get());
        else
            System.out.println("There is no folder with code " + code);
    }

    @Override
    public void readFolder(String name) {
        Optional<Folder> optionalFolder;
        if ((optionalFolder = folders.stream().filter(folder -> folder.name.equals(name)).findFirst()).isPresent())
            System.out.println(optionalFolder.get());
        else
            System.out.printf("There is no folder with name \"%s\" on disk\n", name);
    }

    @Override
    public void allFolders() {
        folders.forEach(System.out::println);
    }

    @Override
    public void allFiles() {
        files.forEach(System.out::println);
    }

    @Override
    public void showDirectory(String name) {
        int code;
        Optional<Folder> optionalFolder;
        if ((optionalFolder = folders.stream().filter(folder -> folder.name.equals(name)).findFirst()).isPresent()) {
            code = optionalFolder.get().code;
            showDirectory(code);
        } else {
            System.out.printf("There is no folder with name \"%s\"\n", name);
        }
    }

    @Override
    public void showDirectory(int code) {
        AtomicBoolean empty = new AtomicBoolean(true);
        files.forEach(file -> {
            if (file.folderCode == code) {
                System.out.println(file);
                empty.set(false);
            }
        });
        if (empty.get())
            System.out.println("<empty>");
    }

    @Override
    public void updateFile(int code) {
        Optional<File> optionalFile;
        if ((optionalFile = files.stream().filter(file -> file.code == code).findFirst()).isPresent()) {
            while (true) {
                System.out.print("choose the attribute to update: ");
                String attr = scanner.nextLine();
                switch (attr) {
                    case "name":
                        while (true) {
                            scanner = new Scanner(System.in);
                            System.out.print("Enter new name: ");
                            String name = scanner.nextLine();
                            try {
                                Path.of(name);
                                optionalFile.get().name = name;
                                break;
                            } catch (InvalidPathException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case "size":
                        while (true) {
                            System.out.print("New value for size: ");
                            try {
                                scanner = new Scanner(System.in);
                                int size = scanner.nextInt();
                                if (size < 0) throw new InputMismatchException();
                                optionalFile.get().size = size;
                                break;
                            } catch (InputMismatchException e) {
                                System.out.println("Enter a non-negative integer");
                            }
                        }
                        break;
                    case "folder id":
                        while (true) {
                            System.out.print("Enter new folder id: ");
                            try {
                                scanner = new Scanner(System.in);
                                int folderCode = scanner.nextInt();
                                if (folders.stream().noneMatch(folder -> folder.code == folderCode)) {
                                    System.out.printf("There is no folder with an id %d\n", folderCode);
                                    if (!askUser("Do you want to enter another folder id?"))
                                        break;
                                } else if (files.stream().filter(file -> file.folderCode == folderCode).anyMatch(file -> Objects.equals(file.name, optionalFile.get().name))) {
                                    System.out.printf("Folder \"%s\" already contains file \"%s\"\n", folders.stream().filter(folder -> folder.code == folderCode).findFirst().get().name, optionalFile.get().name);
                                } else {
                                    optionalFile.get().folderCode = folderCode;
                                    break;
                                }
                            } catch (InputMismatchException e) {
                                System.out.println("Enter a non-negative integer");
                            }
                        }
                        break;
                    case "visible":
                        while (true) {
                            System.out.print("New value for isVisible: ");
                            try {
                                scanner = new Scanner(System.in);
                                optionalFile.get().isVisible = scanner.nextBoolean();
                                break;
                            } catch (InputMismatchException e) {
                                System.out.println("Enter \"true\" or \"false\"");
                            }
                        }
                        break;
                    case "readable":
                        while (true) {
                            System.out.print("New value for isReadable: ");
                            try {
                                scanner = new Scanner(System.in);
                                optionalFile.get().isReadable = scanner.nextBoolean();
                                break;
                            } catch (InputMismatchException e) {
                                System.out.println("Enter \"true\" or \"false\"");
                            }
                        }
                        break;
                    case "writeable":
                        while (true) {
                            System.out.print("New value for isWriteable: ");
                            try {
                                scanner = new Scanner(System.in);
                                optionalFile.get().isWritable = scanner.nextBoolean();
                                break;
                            } catch (InputMismatchException e) {
                                System.out.println("Enter \"true\" or \"false\"");
                            }
                        }
                        break;
                    default:
                        System.out.printf("""
                                Attribute "%s" is not recognized
                                Attributes of file are:
                                 - name (String)
                                 - size (integer)
                                 - visible (boolean)
                                 - readable (boolean)
                                 - writeable (boolean)
                                 - folder id (integer)
                                """, attr);
                        continue;
                }
                optionalFile.get().lastUpdated = LocalDateTime.now();
                AtomicReference<String> stringAtomicReference = new AtomicReference<>(null);
                folders.stream().filter(folder -> folder.code == optionalFile.get().folderCode).findFirst().ifPresent(folder -> stringAtomicReference.set(folder.name));
                System.out.printf("File \"%s\"/\"%s\" is successfully updated\n", stringAtomicReference.get(), optionalFile.get().name);
                if (!askUser("Do you want to update another attribute of this file?"))
                    break;
            }
        } else {
            System.out.println("There is no file with code " + code);
        }
    }

    @Override
    public void updateFolder(int code) {
        Optional<Folder> optionalFolder;
        if ((optionalFolder = folders.stream().filter(folder -> folder.code == code).findFirst()).isPresent()) {
            while (true) {
                scanner = new Scanner(System.in);
                System.out.print("Enter a new name for this folder: ");
                String name = scanner.nextLine();
                try {
                    Path.of(name + "/file.txt");
                    optionalFolder.get().name = name;
                    break;
                } catch (InvalidPathException e) {
                    System.out.println("Folder name is invalid");
                    if (!askUser("Do you want to try another folder name?"))
                        break;
                }
            }
            System.out.printf("Folder \"%s\" is successfully updated\n", optionalFolder.get().name);
        } else {
            System.out.println("There is no folder with code " + code);
        }
    }

    @Override
    public void deleteFile(int code) {
        files.removeIf(file -> file.code == code);
    }

    @Override
    public void deleteFolder(int code) {
        StringBuilder sb = new StringBuilder("Are you sure you want to delete folder with the following files:\n");
        files.forEach(file -> {
            if (file.folderCode == code) {
                sb.append(" - ").append(file.name).append('\n');
            }
        });
        if (askUser(sb.toString())) {
            files.removeIf(file -> file.folderCode == code);
            folders.removeIf(folder -> folder.code == code);
            System.out.println("Folder is deleted");
        }
    }

    @Override
    public synchronized void saveToFile(String fileName) throws IOException {
        Document document = documentBuilder.newDocument();

        Element root = document.createElement("disk");
        folders.forEach(folder -> root.appendChild(makeFolderElement(document, folder)));
        document.appendChild(root);

        Source source = new DOMSource(document);
        Result fileResult = new StreamResult(new java.io.File(fileName));

        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
        try {
            transformer.transform(source, fileResult);
        } catch (TransformerException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            else
                throw new RuntimeException(e);
        }
    }

    private Element makeFolderElement(Document doc, Folder folder) {
        Element element = doc.createElement("folder");
        element.setAttribute("id", String.valueOf(folder.code));
        element.setAttribute("name", folder.name);

        files.forEach(file -> {
            if (file.folderCode == folder.code)
                element.appendChild(makeFileElement(doc, file));
        });

        return element;
    }

    private Element makeFileElement(Document doc, File file) {
        Element element = doc.createElement("file");
        element.setAttribute("id", String.valueOf(file.code));
        element.setAttribute("folderID", String.valueOf(file.folderCode));
        element.setAttribute("name", file.name);
        element.setAttribute("visible", String.valueOf(file.isVisible));
        element.setAttribute("readable", String.valueOf(file.isReadable));
        element.setAttribute("writeable", String.valueOf(file.isWritable));
        element.setAttribute("size", String.valueOf(file.size));
        String dateTime = file.lastUpdated.toString();
        element.setAttribute("lastUpdated", dateTime.substring(0, (!dateTime.contains(".") ? dateTime.length() : dateTime.indexOf("."))));

        return element;
    }

    @Override
    public void readFromFile(String fileName) throws IOException, SAXException {
        files.clear();
        folders.clear();

        Document document;
        try {
            document = documentBuilder.parse(fileName);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException)
                throw (IOException) e.getException();
            else
                throw e;
        }

        Element root = document.getDocumentElement();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element)
                parseFolder((Element) node);
        }
    }

    private void parseFolder(Element element) {
        Folder folder = new Folder(
                Integer.parseInt(element.getAttribute("id")),
                element.getAttribute("name")
        );
        folders.add(folder);

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node item = children.item(i);
            if (item instanceof Element)
                parseFile((Element) item);
        }
    }

    private void parseFile(Element element) {
        File file = new File(
                Integer.parseInt(element.getAttribute("id")),
                Integer.parseInt(element.getAttribute("folderID")),
                element.getAttribute("name"),
                Boolean.parseBoolean(element.getAttribute("visible")),
                Boolean.parseBoolean(element.getAttribute("readable")),
                Boolean.parseBoolean(element.getAttribute("writable")),
                Integer.parseInt(element.getAttribute("size")),
                LocalDateTime.parse(element.getAttribute("lastUpdated"))
        );
        files.add(file);
    }


}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskA {
    public static final Path filePath = Path.of("/Users/dashulik/Library/Mobile Documents/com~apple~CloudDocs/KNU/5th semester/Distribued Computing/Lab4/src/file.txt");
    /**
     * number of threads which are currently reading from the file
     */
    public static Integer reading = 0;
    /**
     * is there a writer
     */
    public static Boolean writing = false;
    public static final Object manager = new Object();
    public static final Object writer = new Object();
    public static final Object reader = new Object();

    public static void main(String[] args) throws IOException {
        List<Thread> list = new ArrayList<>(12){{
            add(new Thread(new Adder("Суховецький Максим Миколайович - +380 (68) 597 46 00"), "Add1"));
            add(new Thread(new NameFinder("+48 668 648 824"), "FindName1"));
            add(new Thread(new PhoneFinder("Суховецький Максим Миколайович"), "FindPhone1"));
            add(new Thread(new Remover("Панчук Тетьяна Володимирівна"), "Remove1"));
            add(new Thread(new Adder("Гончарук Наталя Леонідіївна - +380 (98) 297 88 74"), "Add2"));
            add(new Thread(new NameFinder("+380 (68) 597 46 00"), "FindName2"));
            add(new Thread(new PhoneFinder("Панченко Ганна Онуфріївна"), "FindPhone2"));
            add(new Thread(new Remover("Суховецький Максим Миколайович"), "Remove2"));
            add(new Thread(new Adder("Потовський Сергій Миколайович - +380 (67) 433 04 83"), "Add3"));
            add(new Thread(new NameFinder("+380 (73) 505 20 09"), "FindName3"));
            add(new Thread(new PhoneFinder("Слюсарев Володимир Олександрович"), "FindPhone3"));
            add(new Thread(new Remover("Миргородський Артем Олегович"), "Remove3"));
        }};
        list.forEach(Thread::start);
    }
}

class PhoneFinder implements Runnable {
    private final Path filePath = TaskA.filePath;
    private final String name;

    public PhoneFinder(String name) {
        this.name = name;
    }

    static void waitWriterToFinish() {
        while (true) {
            synchronized (TaskA.writer) {
                if (TaskA.writing) {
                    try {
                        TaskA.writer.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            synchronized (TaskA.manager) {
                if (!TaskA.writing) {
                    TaskA.reading++;
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        waitWriterToFinish();
        findUserByName();
        synchronized (TaskA.reader) {
            TaskA.reading--;
            if (TaskA.reading == 0)
                TaskA.reader.notifyAll();
        }
    }

    private void findUserByName() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var found = false;
        for (var line : lines) {
            var nf = new NamePhone(line);
            if (Objects.equals(name, nf.name)) {
                System.out.printf("Потік %s знайшов номер телефону %s за користувачем %s\n", Thread.currentThread().getName(), nf.phone, nf.name);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.printf("Потік %s не знайшов користувача %s\n", Thread.currentThread().getName(), name);
        }
    }
}

class NameFinder implements Runnable {
    private final Path filePath = TaskA.filePath;
    private final String phoneNumber;

    public NameFinder(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void run() {
        PhoneFinder.waitWriterToFinish();
        findUserByPhoneNumber();
        synchronized (TaskA.reader) {
            TaskA.reading--;
            if (TaskA.reading == 0)
                TaskA.reader.notifyAll();
        }
    }

    private void findUserByPhoneNumber() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var found = false;
        for (var line : lines) {
            var nf = new NamePhone(line);
            if (Objects.equals(phoneNumber, nf.phone)) {
                System.out.printf("Потік %s знайшов користувача %s за номером телефону %s\n", Thread.currentThread().getName(), nf.name, nf.phone);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.printf("Потік %s не знайшов номер телефону %s\n", Thread.currentThread().getName(), phoneNumber);
        }
    }
}

class Remover implements Runnable {
    private final Path filePath = TaskA.filePath;
    private final String name;

    public Remover(String name) {
        this.name = name;
    }

    static void waitReadersAndOtherWritersToFinish() {
        while (true) {
            synchronized (TaskA.writer) {
                if (TaskA.writing) {
                    try {
                        TaskA.writer.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            synchronized (TaskA.reader) {
                if (TaskA.reading > 0) {
                    try {
                        TaskA.reader.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            synchronized (TaskA.manager) {
                if (!TaskA.writing && TaskA.reading == 0) {
                    TaskA.writing = true;
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        waitReadersAndOtherWritersToFinish();
        removeUser();
        // синхронізація надлишкова оскільки зараз виконується лише 1 потік
        synchronized (TaskA.writer) {
            TaskA.writing = false;
            TaskA.writer.notifyAll();
        }
    }

    private void removeUser() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < lines.size(); i++) {
            var nf = new NamePhone(lines.get(i));
            if (Objects.equals(nf.name, name)) {
                lines.remove(i);
                System.out.printf("Потік %s видалив користувача користувача %s\n", Thread.currentThread().getName(), name);
                break;
            }
            if (i == lines.size() - 1) {
                System.out.printf("Потік %s не знайшов користувача %s\n", Thread.currentThread().getName(), name);
            }
        }
        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Adder implements Runnable {
    private final Path filePath = TaskA.filePath;
    private final String toAdd;

    public Adder(String toAdd) {
        this.toAdd = toAdd;
    }

    @Override
    public void run() {
        Remover.waitReadersAndOtherWritersToFinish();
        addLine();
        synchronized (TaskA.writer) {
            TaskA.writing = false;
            TaskA.writer.notifyAll();
        }
    }

    private void addLine() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lines.add(toAdd);
        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Потік %s додав запис %s\n", Thread.currentThread().getName(), toAdd);
    }
}

class NamePhone {
    String phone, name;
    NamePhone(String str) {
        var arr = str.split(" - ");
        name = arr[0];
        phone = arr[1];
    }
}
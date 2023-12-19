import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TaskB {
    static final int N = 4;
    static final Path filePath = Path.of("/Users/dashulik/Library/Mobile Documents/com~apple~CloudDocs/KNU/5th semester/Distribued Computing/Lab4/src/output.txt");
    static final Garden garden = new Garden();
    public static void main(String[] args) {
        List<Thread> list = new ArrayList<>(4){{
            add(new Thread(new Console()));
            add(new Thread(new Gardener()));
            add(new Thread(new File()));
            add(new Thread(new Nature()));
        }};
        list.forEach(Thread::start);
    }
}

class Garden {
    ReentrantReadWriteLock.ReadLock readLock;
    ReentrantReadWriteLock.WriteLock writeLock;
    {
        var lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    int[][] array = {
            {1, 0, 1, 1, 0},
            {0, 0, 1, 1, 1},
            {1, 1, 0, 1, 1},
            {0, 1, 0, 0, 0},
            {1, 1, 0, 0, 1}
    };

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>(6);
        for (var row : array) {
            List<String> chars = new ArrayList<>(6);
            for (var c : row) {
                if (c == 0) chars.add("🥀");
                else chars.add("🌹");
            }
            lines.add(String.join(" ", chars));
        }
        lines.add("- - - - - - - -\n");

        return String.join("\n", lines);
    }
}

class Gardener implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < TaskB.N; i++) {
            TaskB.garden.writeLock.lock();
            for (int j = 0; j < TaskB.garden.array.length; j++) {
                Arrays.fill(TaskB.garden.array[j], 1);
            }
            TaskB.garden.writeLock.unlock();
        }
    }
}

class Nature implements Runnable {
    Random random = new Random(System.currentTimeMillis());
    @Override
    public void run() {
        for (int i = 0; i < TaskB.N; i++) {
            TaskB.garden.writeLock.lock();
            for (int j = 0; j < TaskB.garden.array.length; j++) {
                for (int k = 0; k < TaskB.garden.array[j].length; k++) {
                    TaskB.garden.array[j][k] = random.nextInt() % 2;
                }
            }
            TaskB.garden.writeLock.unlock();
        }
    }
}

class Console implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < TaskB.N; i++) {
            TaskB.garden.readLock.lock();
            System.out.print(TaskB.garden);
            TaskB.garden.readLock.unlock();
        }
    }
}

class File implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < TaskB.N; i++) {
            TaskB.garden.readLock.lock();
            try {
                Files.write(
                        TaskB.filePath,
                        TaskB.garden.toString().getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                TaskB.garden.readLock.unlock();
            }
        }
    }
}

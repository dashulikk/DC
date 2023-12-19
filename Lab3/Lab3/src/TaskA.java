import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class TaskA {
    public static void main(String[] args) throws InterruptedException {
        int n = 9;
        int N = 30;
        Semaphore sem = new Semaphore(1);
        sem.acquire();
        Pot pot = new Pot(N);
        List<Thread> bees = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            bees.add(new Thread(new Bee(sem, pot, i + 1)));
        }
        Thread bear = new Thread(new Bear(sem, pot));
        bees.forEach(Thread::start);
        bear.start();
    }
}

class Pot {
    private final int fullVolume;
    private int volume;

    public Pot(int fullVolume) {
        this.fullVolume = fullVolume;
    }

    public void add(int vol) {
        volume += vol;
        if (volume > fullVolume)
            volume = fullVolume;
    }

    public int getVolume() {
        return this.volume;
    }

    public boolean isFull() {
        return volume == fullVolume;
    }

    public void eatAll() {
        volume = 0;
    }
}

class Bee implements Runnable {
    private final static Random random = new Random(System.currentTimeMillis());
    private final Semaphore sem;
    private final Pot pot;
    private final int num;

    public Bee(Semaphore sem, Pot pot, int num) {
        this.sem = sem;
        this.pot = pot;
        this.num = num;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (pot) {
                if (!pot.isFull()) {
                    if (!pot.isFull()) {
                        pot.add(1);
                        System.out.printf("🐝[%d] 🍯 -> %d%s\n", num, pot.getVolume(), pot.isFull() ? " (full)" : "");
                    }
                    if (pot.isFull())
                        sem.release(1);
                }
            }
            try {
                Thread.sleep(500 + random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Bear implements Runnable {
    private final Semaphore sem;
    private final Pot pot;

    public Bear(Semaphore sem, Pot pot) {
        this.sem = sem;
        this.pot = pot;
    }

    @Override
    public void run() {
        while (true) {
            try {
                sem.acquire();
                pot.eatAll();
                System.out.println("🐻 Ведмідь з'їдає весь мед 🍯");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

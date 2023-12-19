import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;

public class TaskC {
    public static void main(String[] args) {
        ConcurrentLinkedQueue<Items> queue = new ConcurrentLinkedQueue<>();
        Object isSmoking = new Object();
        int count = 10;
        Phaser phaser = new Phaser();
        Broker broker = new Broker(queue, isSmoking, count);
        Smoker withPaper = new Smoker(queue, isSmoking, count, Item.PAPER, phaser);
        Smoker withTobacco = new Smoker(queue, isSmoking, count, Item.TOBACCO, phaser);
        Smoker withMatches = new Smoker(queue, isSmoking, count, Item.MATCHES, phaser);
        new Thread(broker).start();
        new Thread(withPaper).start();
        new Thread(withTobacco).start();
        new Thread(withMatches).start();
    }
}

enum Items {
    WITHOUT_TOBACCO,
    WITHOUT_MATCHES,
    WITHOUT_PAPER,
}

class Broker implements Runnable {
    public final static Map<Items, String> map;
    private final Random random = new Random(System.currentTimeMillis());
    private final ConcurrentLinkedQueue<Items> queue;
    public final static Semaphore sem;
    private final Object isSmoking;
    private final int count;

    static {
        map = new HashMap<>(){{
            put(Items.WITHOUT_MATCHES, "папір і табак");
            put(Items.WITHOUT_PAPER, "табак і сірники");
            put(Items.WITHOUT_TOBACCO, "папір та сірники");
        }};
        sem = new Semaphore(3);
        try {
            sem.acquire(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Broker(ConcurrentLinkedQueue<Items> queue, Object isSmoking, int count) {
        this.queue = queue;
        this.isSmoking = isSmoking;
        this.count = count;
    }

    private Items getRandItems() {
        return switch (random.nextInt() % 3) {
            case 0 -> Items.WITHOUT_TOBACCO;
            case 1 -> Items.WITHOUT_MATCHES;
            default -> Items.WITHOUT_PAPER;
        };
    }

    @Override
    public void run() {
        Items items = getRandItems();
        queue.add(items);
        sem.release(3);
        System.out.printf("Посередник поклав на стіл %s\n", map.get(items));
        for (int i = 1; i < count; i++) {
            try {
                synchronized (isSmoking) {
                    isSmoking.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("❌ Broker is interrupted");
            }
            items = getRandItems();
            queue.add(items);
            System.out.printf("Посередник поклав на стіл %s\n", map.get(items));
            sem.release(3);
        }
    }
}

enum Item {
    PAPER,
    TOBACCO,
    MATCHES
}

class Smoker implements Runnable {
    private static final Map<Item, String> map1;
    private final Phaser phaser;
    private final ConcurrentLinkedQueue<Items> queue;
    private final Object isSmoking;
    private final int count;
    private final Item item;

    static {
        map1 = new HashMap<>(){{
            put(Item.PAPER, "папером");
            put(Item.TOBACCO, "табаком");
            put(Item.MATCHES, "сірниками");
        }};
    }

    public Smoker(ConcurrentLinkedQueue<Items> queue, Object isSmoking, int count, Item item, Phaser phaser) {
        this.queue = queue;
        this.isSmoking = isSmoking;
        this.count = count;
        this.item = item;
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            try {
                Broker.sem.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("❌ Потік курця, у якого є %s перервано", item);
            }
            Items items = queue.peek();
            System.out.printf("Курець з %s поглянув на стіл\n", map1.get(item));
            phaser.arriveAndAwaitAdvance();
            switch (items) {
                case WITHOUT_MATCHES:
                    if (item == Item.MATCHES) {
                        System.out.printf("\nКурець з %s взяв табак і папір\n🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬\nКурець з %s закінчив курити\n\n", map1.get(item), map1.get(item));
                        queue.poll();
                        synchronized (isSmoking) {
                            isSmoking.notify();
                        }
                    }
                    break;
                case WITHOUT_PAPER:
                    if (item == Item.PAPER) {
                        System.out.printf("\nКурець з %s взяв табак і сірники\n🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬\nКурець з %s закінчив курити\n\n", map1.get(item), map1.get(item));
                        queue.poll();
                        synchronized (isSmoking) {
                            isSmoking.notify();
                        }
                    }
                    break;
                case WITHOUT_TOBACCO:
                    if (item == Item.TOBACCO) {
                        System.out.printf("\nКурець з %s взяв папір і сірники\n🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬 🚬\nКурець з %s закінчив курити\n\n", map1.get(item), map1.get(item));
                        queue.poll();
                        synchronized (isSmoking) {
                            isSmoking.notify();
                        }
                    }
                    break;
                default:
                    throw new NullPointerException("Посередник ще не поклав нічого на стіл");
            }
            phaser.arriveAndAwaitAdvance();
        }
    }
}
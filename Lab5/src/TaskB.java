import java.security.SecureRandom;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;


public class TaskB {
    public static void main(String[] args) throws InterruptedException {
        Phaser phaser = new Phaser();
        Thread t1 = new Thread(new Changer(new StringBuilder("ABCDDABDA"), phaser));
        Thread t2 = new Thread(new Changer(new StringBuilder("ABDDDACDA"), phaser));
        Thread t3 = new Thread(new Changer(new StringBuilder("DBCBCBDAB"), phaser));
        Thread t4 = new Thread(new Changer(new StringBuilder("CDDADBCDD"), phaser));
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        System.out.println("Number of cycles = " + (phaser.getPhase() / 2));
    }
}

class Changer implements Runnable {
    private final SecureRandom random = new SecureRandom();
    private static final AtomicInteger numberOfAB = new AtomicInteger(-1);
    private static boolean first;
    private static int equals = 0;
    private static boolean loop = true;
    private final StringBuilder str;
    private final Phaser phaser;

    public Changer(StringBuilder str, Phaser phaser) {
        this.str = str;
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {
        while (loop) {
            numberOfAB.set(-1);
            for (int i = 0; i < str.length(); i++) {
                if (random.nextBoolean()) {
                    switch (str.charAt(i)) {
                        case 'A' -> str.setCharAt(i, 'C');
                        case 'B' -> str.setCharAt(i, 'D');
                        case 'C' -> str.setCharAt(i, 'A');
                        case 'D' -> str.setCharAt(i, 'B');
                    }
                }
            }
            int count = str.chars().reduce(0, (subtotal, element) -> {
                if (element == 'A' || element == 'B')
                    return subtotal + 1;
                return subtotal;
            });
            phaser.arriveAndAwaitAdvance();
            synchronized (numberOfAB) {
                if (numberOfAB.get() == -1) {
                    first = true;
                    numberOfAB.set(count);
                    equals = 1;
                } else if (numberOfAB.get() == count) {
                    equals++;
                } else if (equals == 1 && first) {
                    first = false;
                } else if (equals == 3) {
                    loop = false;
                }
            }
            phaser.arriveAndAwaitAdvance();
        }
    }
}
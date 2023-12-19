import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/*
DEADLOCK
OR
LIVELOCK
 */

public class TaskA {
    static int nThreads;
    static int nRecruits = 100;
    static int capacity = 50;
    static int onBarrier = 0;
    static final Object barrier = new Object();
    static final Object stationaryState = new Object();

    public static void main(String[] args) throws InterruptedException {
        var rp = randomRecruits();
        print(rp);
        nThreads = (int) Math.round((double)nRecruits / capacity);
        System.out.println("Number of threads = " + nThreads);
        List<Thread> list = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads - 1; i++) {
            list.add(new Thread(new Switcher(rp, i * capacity, i * capacity + capacity)));
        }
        list.add(new Thread(new Switcher(rp, (nThreads - 1) * capacity, nRecruits)));
        list.forEach(Thread::start);
        synchronized (stationaryState) {
            try {
                stationaryState.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        print(rp);
    }

    static RecruitPosition[] randomRecruits() {
        Random random = new Random(System.currentTimeMillis());
        RecruitPosition[] r = new RecruitPosition[nRecruits];
        for (int i = 0; i < nRecruits; i++) {
            r[i] = random.nextInt() % 2 == 0 ? RecruitPosition.LEFT : RecruitPosition.RIGHT;
        }
        return r;
    }

    static void print(RecruitPosition[] rp) {
        System.out.println(Arrays.stream(rp).map(p -> p == RecruitPosition.LEFT ? "L" : "R").collect(Collectors.joining("")));
    }
}

enum RecruitPosition {
    LEFT,
    RIGHT
}

class Switcher implements Runnable {
    private RecruitPosition[] rp;
    private int start;
    private int end;
    private static boolean stationaryState = false;

    public Switcher(RecruitPosition[] rp, int start, int end) {
        this.rp = rp;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        do {
            boolean lastTurned = false;
            for (int i = start; i < end - 1; i++) {
                if (i == start) {
                    synchronized (rp[i]) {
                        if (rp[i] == RecruitPosition.RIGHT && rp[i + 1] == RecruitPosition.LEFT) {
                            rp[i] = RecruitPosition.LEFT;
                            rp[i + 1] = RecruitPosition.RIGHT;
                            i++;
                            if (i + 1 == end - 1) lastTurned = true;
                        }
                    }
                } else {
                    if (rp[i] == RecruitPosition.RIGHT && rp[i + 1] == RecruitPosition.LEFT) {
                        rp[i] = RecruitPosition.LEFT;
                        rp[i + 1] = RecruitPosition.RIGHT;
                        i++;
                        if (i == end - 1) lastTurned = true;
                    }
                }
            }

            if (end != rp.length && !lastTurned) {
                synchronized (rp[end]) {
                    if (rp[end - 1] == RecruitPosition.RIGHT && rp[end] == RecruitPosition.LEFT) {
                        rp[end - 1] = RecruitPosition.LEFT;
                        rp[end] = RecruitPosition.RIGHT;
                    }
                }
            }
            synchronized (TaskA.barrier) {
                TaskA.onBarrier++;
                if (TaskA.onBarrier != TaskA.nThreads) {
                    try {
                        TaskA.barrier.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    TaskA.onBarrier = 0;
                    stationaryState = stationaryState();
                    TaskA.barrier.notifyAll();
                }
            }
        } while (!stationaryState);
        synchronized (TaskA.stationaryState) {
            if (--TaskA.nThreads == 0) {
                TaskA.stationaryState.notify();
            }
        }
    }

    private synchronized boolean stationaryState() {
        for (int i = 1; i < rp.length; i++) {
            if (rp[i - 1] == RecruitPosition.RIGHT && rp[i] == RecruitPosition.LEFT) {
                return false;
            }
        }
        return true;
    }
}


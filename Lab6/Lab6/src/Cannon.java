import java.util.ArrayList;
import java.util.List;

public class Cannon {
    CannonMatrix matrix;

    Cannon(int m){
        matrix = new CannonMatrix(m);
        matrix.init();
    }

    public void multiply() {
        matrix.shiftLeft();
        matrix.shiftUp();
        startSetThreads();
        for (int i = 1; i < this.matrix.numberBlocks; i++) {
            matrix.shiftLeftSimple();
            matrix.shiftUpSimple();
            startSetThreads();
        }
    }

    private void startSetThreads() {
        List<Thread> list = new ArrayList<>(16);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                list.add(new Task(matrix, i, j));
            }
        }
        list.forEach(Thread::start);
        try {
            for (Thread thread : list) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}

class Task extends Thread {
    CannonMatrix matrix;
    int x;
    int y;
    public Task(CannonMatrix matrix, int x, int y) {
        this.matrix = matrix;
        this.x = x;
        this.y = y;
    }
    @Override
    public void run() {
        int[][] C = matrix.multiply(matrix.getA()[x][y], matrix.getB()[x][y]);
        matrix.addition(C, x, y);
    }
}
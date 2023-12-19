import java.util.ArrayList;
import java.util.List;

public class RibbonDiagram {
    private int[][] A;
    private int[][] B;
    private int[][] C;
    private List<Thread> threads;

    public RibbonDiagram(int nThreads, int[][] a, int[][] b) {
        A = a;
        B = b;
        C = new int[a.length][b[0].length];
        threads = new ArrayList<>(nThreads);
    }

    public synchronized void add(Thread t) {
        threads.add(t);
    }

    public int[][] multiply() {
        threads.forEach(Thread::start);
        try {
            for (var t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return C;
    }

    class Task implements Runnable {
        // first and last row index of matrix A
        private final int start;
        private final int end;

        public Task(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int row = start; row < end; row++) {
                for (int col = 0; col < B[0].length; col++) {
                    C[row][col] = multiplyMatricesCell(A, B, row, col);
                }
            }
        }

        int multiplyMatricesCell(int[][] A, int[][] B, int row, int col) {
            int cell = 0;
            for (int i = 0; i < B.length; i++) {
                cell += A[row][i] * B[i][col];
            }
            return cell;
        }
    }
}
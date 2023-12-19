import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FoxAlgorithm {
    int[][] A;
    int[][] B;
    int nThreads;

    public FoxAlgorithm(int[][] a, int[][] b, int nThreads) {
        A = a;
        B = b;
        this.nThreads = nThreads;
    }

    int[][] multiply() {
        int[][] C = new int[A.length][B[0].length];

        int step = A.length / nThreads;

        ExecutorService exec = Executors.newFixedThreadPool(nThreads);
        ArrayList<Future<?>> threads = new ArrayList<>();

        int[][] matrixOfSizesI = new int[nThreads][nThreads];
        int[][] matrixOfSizesJ = new int[nThreads][nThreads];

        int stepI = 0;
        for (int i = 0; i < nThreads; i++) {
            int stepJ = 0;
            for (int j = 0; j < nThreads; j++) {
                matrixOfSizesI[i][j] = stepI;
                matrixOfSizesJ[i][j] = stepJ;
                stepJ += step;
            }
            stepI += step;
        }

        for (int l = 0; l < nThreads; l++) {
            for (int i = 0; i < nThreads; i++) {
                for (int j = 0; j < nThreads; j++) {
                    int stepI0 = matrixOfSizesI[i][j];
                    int stepJ0 = matrixOfSizesJ[i][j];

                    int stepI1 = matrixOfSizesI[i][(i + l) % nThreads];
                    int stepJ1 = matrixOfSizesJ[i][(i + l) % nThreads];

                    int stepI2 = matrixOfSizesI[(i + l) % nThreads][j];
                    int stepJ2 = matrixOfSizesJ[(i + l) % nThreads][j];

                    FoxAlgorithmThread t =
                            new FoxAlgorithmThread(
                                    copyBlock(A, stepI1, stepJ1, step),
                                    copyBlock(B, stepI2, stepJ2, step),
                                    C,
                                    stepI0,
                                    stepJ0);
                    threads.add(exec.submit(t));
                }
            }
        }

        for (var mapFuture : threads) {
            try {
                mapFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        exec.shutdown();

        return C;
    }

    private int[][] copyBlock(int[][] a, int i, int j, int n) {
        int[][] block = new int[n][n];
        for (int k = 0; k < n; k++) {
            System.arraycopy(a[k + i], j, block[k], 0, n);
        }
        return block;
    }
}
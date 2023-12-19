import mpi.*;

import java.security.SecureRandom;

public class ParallelMatrixMultiplication {
    static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) throws InterruptedException {
        int n = 1000;
        var A = randomMatrix(n, n);
        var B = randomMatrix(n, n);
        linear(args, A, B);
        ribbonDiagram(args, A, B);
        foxAlgorithm(args, A, B);
        cannonAlgorithm(args, n);
    }

    static void linear(String[] args, int[][] A, int[][] B) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        if (rank == 0) {
            long begin = System.currentTimeMillis();
            LinearMatrixMultiplication.mult(A, B);
            long time = System.currentTimeMillis() - begin;
            System.out.println("Linear time = " + time + " milliseconds");
        }
        MPI.Finalize();
    }

    static void ribbonDiagram(String[] args, int[][] A, int[][] B) throws InterruptedException {

        int n = A.length;
        int size = Integer.parseInt(args[1]);
        RibbonDiagram ribbon;
        ribbon = new RibbonDiagram(size, A, B);
        int rows = n / size;
        for (int i = 0; i < size - 1; i++) {
            ribbon.add(new Thread(ribbon.new Task(i * rows, i * rows + rows)));
        }
        ribbon.add(new Thread(ribbon.new Task((size - 1) * rows, n)));

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        long begin = 0;
        if (rank == 0) begin = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            if (rank == i) {
                Thread t = new Thread(ribbon.new Task(i * rows, i == size - 1 ? n : i * rows + rows));
                ribbon.add(t);
                t.start();
                t.join();
            }
        }
        MPI.Finalize();
        if (rank == 0) {
            long time = System.currentTimeMillis() - begin;
            System.out.println("Ribbon diagram time = " + time + " milliseconds");
        }
    }

    static void foxAlgorithm(String[] args, int[][] A, int[][] B) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        FoxAlgorithm fox = new FoxAlgorithm(A, B, MPI.COMM_WORLD.Size());
        long time = 0;
        if (rank == 0) time = System.currentTimeMillis();
        fox.multiply();
        if (rank == 0) {
            time = System.currentTimeMillis() - time;
            System.out.println("Fox algorithm time = " + time + " milliseconds");
        }
        MPI.Finalize();
    }

    static void cannonAlgorithm(String[] args, int n) {
        Cannon cannon = new Cannon(n);
        MPI.Init(args);
        long time = System.currentTimeMillis();
        cannon.multiply();
        time = System.currentTimeMillis() - time;
        if (MPI.COMM_WORLD.Rank() == 0) {
            System.out.println("Cannon algorithm time = " + time + " milliseconds");
        }
        MPI.Finalize();
    }

    static int[][] randomMatrix(int rows, int cols) {
        int[][] A = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                A[i][j] = random.nextInt(200) - 100;
            }
        }
        return A;
    }
}
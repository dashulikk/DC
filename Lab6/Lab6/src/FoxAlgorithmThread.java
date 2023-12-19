public class FoxAlgorithmThread extends Thread {
    private final int[][] A;
    private final int[][] B;
    private final int[][] C;

    private final int stepI;
    private final int stepJ;

    public FoxAlgorithmThread(int[][] a, int[][] b, int[][] c, int stepI, int stepJ) {
        A = a;
        B = b;
        C = c;
        this.stepI = stepI;
        this.stepJ = stepJ;
    }

    @Override
    public void run() {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                int elemIJ = 0;
                for (int k = 0; k < B.length; k++) {
                    elemIJ += A[i][k] * B[k][j];
                }
                C[i + stepI][j + stepJ] = elemIJ;
            }
        }
    }
}
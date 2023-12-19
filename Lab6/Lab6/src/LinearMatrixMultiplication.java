public class LinearMatrixMultiplication {
    public static int[][] mult(int[][] A, int[][] B) {
        int[][] C = new int[A.length][B[0].length];
        for (int row = 0; row < A.length; row++) {
            for (int col = 0; col < B[0].length; col++) {
                C[row][col] = multiplyMatricesCell(A, B, row, col);
            }
        }
        return C;
    }

    private static int multiplyMatricesCell(int[][] A, int[][] B, int row, int col) {
        int cell = 0;
        for (int i = 0; i < B.length; i++) {
            cell += A[row][i] * B[i][col];
        }
        return cell;
    }
}
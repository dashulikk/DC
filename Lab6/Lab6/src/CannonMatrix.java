import java.util.Random;

public class CannonMatrix {
    public final int numberBlocks = 4;
    private int[][][][] A;
    private int[][][][] B;
    private int[][][][] C;
    int sizeBlock;

    public CannonMatrix(int m) {
        this.sizeBlock = m/numberBlocks;
    }

    public void init() {
        A = new int[numberBlocks][numberBlocks][sizeBlock][sizeBlock];
        B = new int[numberBlocks][numberBlocks][sizeBlock][sizeBlock];
        C = new int[numberBlocks][numberBlocks][sizeBlock][sizeBlock];
        Random rand = new Random();
        for (int i = 0; i < numberBlocks; i++) {
            for (int j = 0; j < numberBlocks; j++) {
                for (int k = 0; k < sizeBlock; k++) {
                    for (int l = 0; l < sizeBlock; l++) {
                        A[i][j][k][l] = rand.nextInt(10);
                        B[i][j][k][l] = rand.nextInt(10);
                    }
                }
            }
        }
    }

    public void shiftLeft() {
        int[][][][] tempRow = new int [1][numberBlocks][sizeBlock][sizeBlock];
        for (int i = 0; i < numberBlocks; i++) {
            System.arraycopy(A[i], 0, tempRow[0], 0, i);
            for (int j = 0; j < numberBlocks-i; j++) {
                A[i][j] = A[i][j+i];
            }
            int k = 0;
            for (int j = numberBlocks-i; j < numberBlocks; j++) {
                A[i][j] = tempRow[0][k];
                k++;
            }
        }
    }

    public void shiftUp() {
        int[][][][] tempColumn = new int [numberBlocks][1][sizeBlock][sizeBlock];
        for (int j = 0; j < numberBlocks; j++) {
            for (int i = 0; i < j; i++) {
                tempColumn[i][0] = B[i][j];
            }
            for (int i = 0; i < numberBlocks-j; i++) {
                B[i][j] = B[i+j][j];
            }
            int k = 0;
            for (int i = numberBlocks-j; i < numberBlocks; i++) {
                B[i][j] = tempColumn[k][0];
                k++;
            }
        }
    }

    public void shiftLeftSimple() {
        int[][] tempBlock;
        for (int i = 0; i < numberBlocks; i++) {
            tempBlock = A[i][0];
            for (int j = 0; j < numberBlocks-1; j++) {
                A[i][j] = A[i][j+1];
            }
            A[i][numberBlocks-1] = tempBlock;
        }
    }

    public void shiftUpSimple() {
        int[][] tempBlock;
        for (int j = 0; j < numberBlocks; j++) {
            tempBlock = B[0][j];
            for (int i = 0; i < numberBlocks-1; i++) {
                B[i][j] = B[i+1][j];
            }
            B[numberBlocks-1][j] = tempBlock;
        }
    }

    public int[][] multiply(int[][] D, int[][] E) {
        int[][] F = new int[D.length][D.length];
        for (int i = 0; i < D.length; i++) {
            for (int j = 0; j < D.length; j++) {
                for (int k = 0; k < D.length; k++) {
                    F[i][j] = F[i][j] + D[i][k] * E[k][j];
                }
            }
        }
        return F;
    }

    public void addition(int[][] C, int x, int y){
        for (int i = 0; i < sizeBlock; i++) {
            for (int j = 0; j < sizeBlock; j++) {
                this.C[x][y][i][j] = this.C[x][y][i][j] + C[i][j];
            }
        }
    }

    public int[][][][] getA(){
        return A;
    }

    public int[][][][] getB(){
        return B;
    }
}
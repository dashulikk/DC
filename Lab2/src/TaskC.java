import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TaskC {
    public static void main(String[] args) {
        WinnerSelector selector = new WinnerSelector();
        ForkJoinPool pool = new ForkJoinPool();
        int winner = pool.invoke(selector);
        System.out.printf("The monk number %d wins the competition with %.2f of Qi energy", winner, WinnerSelector.participants[winner]);
    }
}

class WinnerSelector extends RecursiveTask<Integer> {
    public static final double[] participants = new double[]{9.2, 8.8, 9.71, 7.94, 7.54, 8.52, 7.72, 8.02,
            9.82, 8.9, 8.23, 8.51, 7.24, 7.45, 7.9, 8.08};
    private final int startIndex;
    private final int endIndex;

    public WinnerSelector() {
        startIndex = 0;
        endIndex = participants.length;
    }

    private WinnerSelector(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     *
     * @return Index of the winner in the competitions
     */
    @Override
    protected Integer compute() {
        if (endIndex - startIndex == 1) {
            return startIndex;
        } else {
            int mid = (endIndex + startIndex) / 2;
            WinnerSelector left = new WinnerSelector(startIndex, mid);
            WinnerSelector right = new WinnerSelector(mid, endIndex);

            left.fork();
            int second = right.compute();
            int first = left.join();

            int winner = participants[first] > participants[second] ? first : second;
            System.out.printf("[%d] <=> [%d] -> %d\n", first, second, winner);

            return winner;
        }
    }
}

import java.util.Arrays;
import java.util.Random;

public class  TaskA {
    public static void main(String[] args) {
        int n = 3;
        Forest forest = new Forest(14);
        SwarmOfBees[] swarm = new SwarmOfBees[n];
        for (int i = 0; i < n; i++) {
            swarm[i] = new SwarmOfBees(i + 1, forest);
        }
        Arrays.stream(swarm).forEach(Thread::start);
    }
}

class ForestArea {
    private int areaNumber;
    private boolean hasWinnieThePooh;

    public ForestArea(int areaNumber, boolean hasWinnieThePooh) {
        this.areaNumber = areaNumber;
        this.hasWinnieThePooh = hasWinnieThePooh;
    }

    public boolean hasWinnieThePooh() {
        return hasWinnieThePooh;
    }

    public int getAreaNumber() {
        return areaNumber;
    }
}

class Forest {
    private ForestArea[] forestAreas;
    private int areaIndex = 0;
    private boolean isWinnieThePoohFound = false;

    public Forest(int numberOfAreas) {
        forestAreas = new ForestArea[numberOfAreas];
        int idx = new Random(System.currentTimeMillis()).nextInt(numberOfAreas);
        for (int i = 0; i < numberOfAreas; i++) {
            forestAreas[i] = idx == i ? new ForestArea(i + 1, true) :
                    new ForestArea(i + 1, false);
        }
    }

    public ForestArea nextArea() {
        if (areaIndex >= forestAreas.length)
            return null;
        else
            return forestAreas[areaIndex++];
    }

    public void setWinnieThePoohFound(boolean winnieThePoohFound) {
        isWinnieThePoohFound = winnieThePoohFound;
    }

    public boolean isWinnieThePoohFound() {
        return isWinnieThePoohFound;
    }
}

class SwarmOfBees extends Thread {
    private final int swarmNumber;
    private final Forest forest;

    public SwarmOfBees(int swarmNumber, Forest forest) {
        this.swarmNumber = swarmNumber;
        this.forest = forest;
    }

    private void goHome() {
        System.out.println("Зграє номер " + swarmNumber + " повертається до вулика");
    }

    @Override
    public void run() {
        ForestArea area;
        while (!forest.isWinnieThePoohFound()) {
            synchronized (forest) {
                area = forest.nextArea();
                if (area == null) break;
            }
            System.out.println("Зграє номер " + swarmNumber + " прочісує ділянку номер " + area.getAreaNumber());
            if (area.hasWinnieThePooh()) {
                System.out.println("Зграє номер " + swarmNumber + " знайшла та покарала Вінні Пуха");
                forest.setWinnieThePoohFound(true);
            } else {
                System.out.println("Зграє номер " + swarmNumber + " не знайшла Вінні Пуха");
            }
            goHome();
        }
    }
}

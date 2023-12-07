package ua.lab8;

import ua.lab8.client.*;

import java.util.Scanner;

public class ClientProcess {

    public static void main(String[] args) {
        Client client = new RMIClient();
        Thread t = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            Controller controller = new Controller(scanner, client);
            View view = new View(controller, scanner);
            view.start();
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

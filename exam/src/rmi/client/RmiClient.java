package rmi.client;

import common.OperationType;
import model.Publication;
import rmi.server.RmiServerRemote;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class RmiClient {

    public static void start(String[] args) {
        try(Scanner scanner = new Scanner(System.in)) {
            RmiServerRemote server = null;
            try {
                server = (RmiServerRemote) Naming.lookup("//localhost:8080/server");
            } catch (NotBoundException | RemoteException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            boolean isStopped = false;

            while(true){
                System.out.println(
                        "Choose option: \n" +
                                "1 - " + OperationType.LINKED_PUBLICATIONS + "\n" +
                                "4 - " + OperationType.CLOSE
                );

                int commandNumber = scanner.nextInt();
                scanner.nextLine();

                List<Publication> publications = null;
                switch (commandNumber) {
                    case 1: {
                        System.out.println("Enter title: ");
                        String title = scanner.nextLine();

                        publications = server.findLinkedPublications(title);
                        break;
                    }
                    case 4: {
                        System.out.println("==> ClientMain stopped");
                        isStopped = true;
                        break;
                    }
                }

                if(isStopped) break;

                System.out.println("Result: ");

                publications.forEach(System.out::println);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

}

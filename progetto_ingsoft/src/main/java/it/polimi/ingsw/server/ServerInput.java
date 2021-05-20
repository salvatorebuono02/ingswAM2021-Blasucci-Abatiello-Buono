package it.polimi.ingsw.server;

import it.polimi.ingsw.server.MainServer;

import java.util.Scanner;

import static java.lang.Thread.sleep;

public class ServerInput implements Runnable {

    @Override
    public void run() {
        System.out.println("sto leggendo da tastiera");
        Scanner scanner=new Scanner(System.in);
        while (true){
            if(scanner.next().equalsIgnoreCase("quit")){
                MainServer.getConnectionServer().setActive(false);
                /*try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                System.out.println("sto chiudendo");
                System.exit(0);
                break;
            }
        }
    }
}
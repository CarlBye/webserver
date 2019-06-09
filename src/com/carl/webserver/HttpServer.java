package com.carl.webserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HttpServer {
    public static void main(String[] args) {
        ServerSocket serverSocket;
        ArrayList<ServerThread> threadList = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("----------Server Start Running----------");
            InetAddress myIp= InetAddress.getLocalHost();
            System.out.println("Server IP："+myIp.getHostAddress());
            System.out.println("Server Port:8080");
            while(true) {
                try {
                    Socket connection = serverSocket.accept();
                    ServerThread threads = new ServerThread(connection);
                    threads.start();
                    threadList.add(threads);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("----------Waiting for threads closing----------");
        for(ServerThread currThread : threadList) {
            try {
                currThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("----------Closing Threads Done----------");
        System.out.println("----------Closing Server Done----------");
    }
}

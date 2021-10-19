package com.geekbrains.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server {

    private final ConcurrentLinkedDeque<ChatHandler> clients;

    public Server(){
        clients = new ConcurrentLinkedDeque<>();
        try (ServerSocket server = new ServerSocket(8189)){
            System.out.println("Server started ...");
            while (true){
                Socket socket = server.accept();
                System.out.println("Client accept");
                ChatHandler handler = new ChatHandler(socket,this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void  broadCastMessage(String message) throws IOException {
        for (ChatHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        new Server();
    }

}

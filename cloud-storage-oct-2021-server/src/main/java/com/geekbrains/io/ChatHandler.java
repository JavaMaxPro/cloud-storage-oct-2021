package com.geekbrains.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatHandler implements Runnable {

    private final Path root;
    private Path clientDir;
    private static int cnt = 0;
    private final String userName;
    private final Socket socket;
    private final Server server;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final SimpleDateFormat format;

    public ChatHandler(Socket socket, Server server) throws IOException {
        root = Path.of("server_root");
        if (!Files.exists(root)) {
            Files.createDirectory(root);
        }

        this.socket = socket;
        this.server = server;
        cnt++;

        userName = "User_" + cnt;
        clientDir = root.resolve(userName);
        if (!Files.exists(clientDir)) {
            Files.createDirectory(clientDir);
        }

        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = dis.readUTF();
                server.broadCastMessage(getMessage(msg));
            }
        } catch (Exception e) {
            System.err.println("Connection was broken");
            e.printStackTrace();
        }
    }

    public String getTime() {
        return format.format(new Date());
    }

    public String getMessage(String msg) {
        return getTime() + " [" + userName + "]" + msg;
    }

    private void responseOK() throws IOException {
        dos.writeUTF("File received!");
        dos.flush();
    }

    public void sendMessage(String msg) throws IOException {
        dos.writeUTF(msg);
        dos.flush();
    }
}

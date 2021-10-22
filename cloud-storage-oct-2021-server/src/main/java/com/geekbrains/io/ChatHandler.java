package com.geekbrains.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatHandler implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    private byte[] buffer;
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
        buffer = new byte[BUFFER_SIZE];
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
                String fileName = dis.readUTF();
                long size = dis.readLong();
                Path path = clientDir.resolve(fileName);
//                Files.copy(dis,path, StandardCopyOption.REPLACE_EXISTING);
                try(FileOutputStream fos = new FileOutputStream(path.toFile())) {
                    for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                        int read = dis.read(buffer);
                        fos.write(buffer,0,read);
                    }
                }
                responseOK();
//                server.broadCastMessage(getMessage(msg));
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

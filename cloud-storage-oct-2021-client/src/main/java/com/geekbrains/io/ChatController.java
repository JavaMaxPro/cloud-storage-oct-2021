package com.geekbrains.io;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChatController implements Initializable {

    private Path root;
    private  byte[] buffer;
    public ListView<String> listView;
    public TextField input;
    private DataOutputStream dos;
    private DataInputStream dis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[1024];
        root = Paths.get("root");
        if (!Files.exists(root)) {
            try {
                Files.createTempDirectory(String.valueOf(root));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
        ;

        try {
            fillFilesInView();
        } catch (Exception e) {
            e.printStackTrace();
        }

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = listView.getSelectionModel().getSelectedItem();
                if (!Files.isDirectory(root.resolve(fileName))) {
                    input.setText(fileName);
                } else {
                    input.setText("Select file! Not directory");
                }
            }
        });
        try {
            Socket socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = dis.readUTF();
                        Platform.runLater(() -> listView.getItems().add(message));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fillFilesInView() throws IOException {
        listView.getItems().clear();
        List<String> list = Files.list(root)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        listView.getItems().addAll(list);
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String fileNasme = input.getText();
        input.clear();
        Path filePath = root.resolve(fileNasme);
        if (Files.exists(filePath)){
            dos.writeUTF(fileNasme);
            dos.writeLong(Files.size(filePath));
            FileInputStream fis = new FileInputStream(filePath.toFile());
            int read = 0;
            while ((read = fis.read(buffer))!=-1){
                dos.write(buffer,0,read);
            }
            fis.close();
//            Files.copy(filePath,dos);
            dos.flush();
        }
       /* dos.writeUTF(message);
        dos.flush();
        input.clear();*/
    }
}

package com.geekbrains.io;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    public ListView<String> listView;
    public TextField input;
    private DataOutputStream dos;
    private DataInputStream dis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            Socket socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(()->{
                try {
                    while (true){
                        String message = dis.readUTF();
                        Platform.runLater(()->listView.getItems().add(message));
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String message = input.getText();
        dos.writeUTF(message);
        dos.flush();
        input.clear();
    }
}

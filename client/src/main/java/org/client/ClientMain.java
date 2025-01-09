package org.client;

import org.client.view.ClientView;

import java.io.IOException;
import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) {
        try{
            Socket socket = new Socket("localhost", 1500);
            ClientView client = new ClientView(socket);
            client.listenForMessage();
            client.sendMessage();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}

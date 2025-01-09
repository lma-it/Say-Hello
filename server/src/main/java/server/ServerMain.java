package server;

import org.hibernate.HibernateException;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerMain {

    public static void main(String[] args) {

        try{
            ServerSocket serverSocket = new ServerSocket(1500);
            Server server = new Server(serverSocket);
            server.runServer();

        }catch (IOException | HibernateException e){
            System.out.println(e.getMessage());
        }

    }
}

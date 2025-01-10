package server;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import server.manager.ClientManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;
    private final Logger logger = (Logger) LoggerFactory.getLogger(Server.class);



    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }


    public void runServer(){
        while (!serverSocket.isClosed()){
            try{
                Socket socket = serverSocket.accept();
                logger.info("Данные о сокете: isConnected: {}, Порт: {}",socket.isConnected(), socket.getPort());
                ClientManager clientManager = new ClientManager(socket);
                logger.info("Новый клиент есть? {}", clientManager);
                System.out.println("Подключен новый клиент!");
                Thread thread = new Thread(clientManager);
                thread.start();
            }catch (IOException e){
                catchErrors(e, "runServer");
                closeSocket();
            }

        }
    }

    private void closeSocket(){
        try{
            if (serverSocket != null) serverSocket.close();
        }catch (IOException e){
            catchErrors(e, "closeSocket");
        }
    }

    private void catchErrors(Exception e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.error("ERROR: в методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }
}

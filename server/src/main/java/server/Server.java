package server;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;
import server.manager.ClientManager;
import server.model.GlobalChat;
import server.repository.impl.GlobalChatRepositoryImpl;
import server.util.SessionService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Slf4j
public class Server {
    private final Logger logger = (Logger) LoggerFactory.getLogger(Server.class);
    private final Session session = SessionService.getSessionFactory(logger, "Server").openSession();
    private final ServerSocket serverSocket;
    private final GlobalChatRepositoryImpl globalChatRepository = new GlobalChatRepositoryImpl(session);


    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }


    public void runServer(){
        while (!serverSocket.isClosed()){
            try{
                List<GlobalChat> globalChat = globalChatRepository.getGlobalChat();
                logger.info("INFO из метода runServer. Состояние globalChat: {}", globalChat);
                if(globalChat.isEmpty()){
                    GlobalChat globalChat1 = new GlobalChat();
                    globalChatRepository.saveEntity(globalChat1);
                    globalChat = globalChatRepository.getGlobalChat();
                    logger.info("INFO из метода runServer. Состояние globalChat: {}", globalChat);
                }
                Socket socket = serverSocket.accept();
                logger.info("Данные о сокете: isConnected: {}, Порт: {}",socket.isConnected(), socket.getPort());
                ClientManager clientManager = new ClientManager(globalChat.getFirst(), socket);
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

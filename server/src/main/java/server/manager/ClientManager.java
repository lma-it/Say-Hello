package server.manager;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;
import server.model.Client;
import server.model.GlobalChat;
import server.model.PrivateChat;
import server.repository.impl.ClientRepositoryImpl;
import server.repository.impl.GlobalChatRepositoryImpl;
import server.repository.impl.PrivateChatRepositoryImpl;
import server.util.SessionService;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class ClientManager implements Runnable{
    private final Logger logger = (Logger) LoggerFactory.getLogger(ClientManager.class);
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ClientRepositoryImpl clientRepository;
    private PrivateChatRepositoryImpl privateChatRepository;
    private GlobalChatRepositoryImpl globalChatRepository;
    private Client client;

    @Getter
    @Setter
    private String login;
    @Setter
    @Getter
    private String userName;
    @Setter
    @Getter
    private String password;
    @Getter
    @Setter
    private String status;
    private final static List<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket){
        this.socket = socket;
        Session session = SessionService.getSession(logger);
        try{
            logger.info("Состояние session: {}", session.getStatistics());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clientRepository = new ClientRepositoryImpl(session);
            privateChatRepository = new PrivateChatRepositoryImpl(session);
            globalChatRepository = new GlobalChatRepositoryImpl(session);
        }catch (IOException e){
            System.out.println(e.getMessage());
            closeEverything(socket, bufferedWriter, bufferedReader);
        }

    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
                if(!messageFromClient.isEmpty())
                    logger.info("Лог из метода run(). Содержание messageFromClient: {}", messageFromClient);
                messageFromClient(messageFromClient);
            }catch (IOException e){
                System.out.println(e.getMessage());
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }

    }

    private void closeEverything(Object... args){
        for(Object obj : args){
            if(obj instanceof AutoCloseable){
                try{
                    ((Closeable) obj).close();
                }catch (IOException e){
                    StringBuilder sb = new StringBuilder();
                    for(StackTraceElement element : e.getStackTrace()){
                        sb.append(element).append("\n");
                    }
                    logger.error("ERROR: Ошибка при попытке закрыть объект: {}. Причина: {}\nStackTrace: {}", obj, e.getMessage(), sb);
                }

            }
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(this.getUserName() + " покинул чат.");
    }

    private void broadcastMessageToClient(ClientManager client, String message){
        try{
            client.bufferedWriter.write(message);
            client.bufferedWriter.newLine();
            client.bufferedWriter.flush();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private void broadcastMessageToClients(List<ClientManager> clients, String message){
        try{
            for(ClientManager client : clients){
                if(!client.getUserName().equals(this.getUserName())){
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    private void messageFromClient(String message){
        String[] user = message.split(":");
        logger.info("Данные о user: {}", message);
        switch (user[0]) {
            case "logins" -> {
                logger.info("Значение в user в case logins: {}", Arrays.toString(user));
                StringBuilder sb = new StringBuilder();
                sb.append("logins").append(":");
                clientRepository.getAllEntities().forEach(client -> sb.append(client.getLogin()).append(":"));
                logger.info("Список всех логинов из БД: {}", sb);
                broadcastMessageToClient(this, String.valueOf(sb));
            }
            case "authorization" -> {
                logger.info("Значение в user в case authorization: {}", Arrays.toString(user));
                user = message.split(":");
                login = user[1];
                userName = user[2];
                password = user[3];
                status = user[4];
                client = new Client(login, userName, password, status);
                clientRepository.saveEntity(client);
                clients.add(this);
                System.out.println("Новый клиент " + userName + " подключился к чату.");
            }
            case "login" -> {
                logger.info("Значение в user в case login: {}", Arrays.toString(user));
                login = user[1];
                password = user[2];
                String slogin;
                client = clientRepository.getEntityByLoginAndPassword(login, password);
                clients.add(this);
                if (client != null) {
                    List<String> globalHistory;
                    globalHistory = globalChatRepository.getEntityById(client.getClientId()).getMessages();
                    if (globalHistory != null) {
                        broadcastMessageToClient(this, String.valueOf(globalHistory));
                    } else {
                        broadcastMessageToClient(this, "Добро пожаловать в чат.");
                    }
                    List<String> privateHistory;
                    privateHistory = privateChatRepository.getEntityById(client.getClientId()).getMessages();
                    if (privateHistory != null) {
                        broadcastMessageToClient(this, String.valueOf(privateHistory));
                    } else {
                        broadcastMessageToClient(this, "Добро пожаловать в приватный чат.");
                    }
                    if (!client.getStatus().equals("Невидимка")) {
                        broadcastMessageToClient(this,"Вы подключились в режиме невидимки.");
                    }
                } else {
                    broadcastMessageToClient(this, "Неверный логин или пароль.");
                }
            }
            case "logout" -> {
                logger.info("Значение в user в case logout: {}", Arrays.toString(user));
                clientRepository.updateEntity(this.client);
                removeClient();
                if(!this.client.getStatus().equals("Невидимка")){
                    broadcastMessageToClients(clients, "Пользователь" + this.client.getName() + " покинул чат.");
                }
                closeEverything(bufferedWriter, bufferedReader);
            }
            case "global" -> {
                GlobalChat globalChat = new GlobalChat();
                globalChat.setClientId(Long.valueOf(user[1]));
                globalChat.setMessages(List.of(user[2]));
                broadcastMessageToClient(this, user[2]);
                if(!this.getStatus().equals("Невидимка")){
                    broadcastMessageToClients(clients, user[2]);
                }
                globalChatRepository.updateEntity(globalChat);
            }
            case "private" -> {
                PrivateChat privateChat = new PrivateChat();
                privateChat.setClientId(Long.valueOf(user[1]));
                privateChat.setMessages(List.of(user[2]));
                privateChatRepository.updateEntity(privateChat);
            }
        }
    }

    @Override
    public String toString() {
        return "ClientManager{" +
                "local port=" + socket.getLocalPort() +
                '}';
    }
}

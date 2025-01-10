package server.manager;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;
import server.model.*;
import server.repository.impl.*;
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
    private PrivateMessageRepositoryImpl privateMessageRepository;
    private GlobalMessageRepositoryImpl globalMessageRepository;
    private Client client;
    private final SessionFactory sessionFactory;

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
        sessionFactory = SessionService.getSessionFactory(logger);
        Session session = sessionFactory.openSession();
        try{
            logger.info("Состояние session: {}", session.getStatistics());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clientRepository = new ClientRepositoryImpl(session);
            privateChatRepository = new PrivateChatRepositoryImpl(session);
            globalChatRepository = new GlobalChatRepositoryImpl(session);
            privateMessageRepository = new PrivateMessageRepositoryImpl(session);
            globalMessageRepository = new GlobalMessageRepositoryImpl(session);
        }catch (IOException e){
            System.out.println(e.getMessage());
            closeEverything(this.socket, bufferedWriter, bufferedReader);
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
                //Пока что просто закомментируем
//                if(!client.getUserName().equals(this.getUserName())){
//
//                }
                client.bufferedWriter.write(message);
                client.bufferedWriter.newLine();
                client.bufferedWriter.flush();
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    private synchronized void messageFromClient(String message){
        String[] requestFromUser = message.split(":");
        logger.info("Данные о user: {}", Arrays.toString(requestFromUser));
        switch (requestFromUser[0]) {
            case "logins" -> {
                logger.info("Значение в user в case logins: {}", Arrays.toString(requestFromUser));
                StringBuilder sb = new StringBuilder();
                sb.append("logins").append(":");
                clientRepository.getAllEntities().forEach(client -> sb.append(client.getLogin()).append(":"));
                logger.info("Список всех логинов из БД: {}", sb);
                broadcastMessageToClient(this, String.valueOf(sb));
            }
            case "authorization" -> {
                logger.info("Значение в user в case authorization: {}", Arrays.toString(requestFromUser));
                requestFromUser = message.split(":");
                login = requestFromUser[1];
                userName = requestFromUser[2];
                password = requestFromUser[3];
                status = requestFromUser[4];
                client = new Client(login, userName, password, status);
                logger.debug("Сохраняем нового клиента в БД: {}", client.toString());
                clientRepository.saveEntity(client);
                client = clientRepository.getEntityByLoginAndPassword(login, password);
                GlobalChat globalChat = new GlobalChat();
                globalChat.setClient(client);
                logger.info("Сохраняем новый общий чат для нового клиента: {}", globalChat);
                globalChatRepository.saveEntity(globalChat);
                PrivateChat privateChat = new PrivateChat();
                privateChat.setClient(client);
                logger.info("Сохраняем новый личный чат для нового клиента: {}", privateChat);
                privateChatRepository.saveEntity(privateChat);
                clients.add(this);
                client.setGlobalChats((List.of(globalChat)));
                client.setPrivateChats(List.of(privateChat));
                clientRepository.updateEntity(client);
                System.out.println("Новый клиент " + userName + " подключился к чату.");
                broadcastMessageToClient(this, String.format("%sВы успешно зарегистрировались. Ваш логин: %s\nИмя: %s\nПароль: %s (не сообщайте пароль никому!)", "authorization", login, userName, password));
            }
            case "login" -> {
                logger.info("Значение в user в case login: {}", Arrays.toString(requestFromUser));
                login = requestFromUser[1];
                password = requestFromUser[2];
                clients.add(this);
                logger.info("Клиенты в списке clients: {}", clients);
                client = clientRepository.getEntityByLoginAndPassword(login, password);
                String id, name, status;
                id = String.valueOf(client.getClientId());
                name = client.getName();
                status = client.getStatus();
                String clientData = "login:" + id + ":" + login + ":" + name + ":" + password + ":" + status;
                broadcastMessageToClient(this, clientData);
                if (client != null) {
                    GlobalChat globalHistory;
                    globalHistory = globalChatRepository.getChatByClient(client);

                    List<String> globalMessages = globalHistory.getMessages() != null ?
                             globalHistory.getMessages().stream()
                            .map(GlobalMessage::getMessage)
                            .toList() : null;

                    broadcastMessageToClient(this, "global:" + globalMessages);
                    PrivateChat privateHistory;
                    privateHistory = privateChatRepository.getChatByClient(client);

                    List<String> privateMessages = privateHistory.getMessages() != null ?
                            privateHistory.getMessages().stream()
                            .map(PrivateMessage::getMessage)
                            .toList() : null;
                    broadcastMessageToClient(this, "private:" + privateMessages);


                    if (client.getStatus().equals("Невидимка")) {
                        broadcastMessageToClient(this,"private:Вы подключились в режиме невидимки.");
                    }
                } else {
                    broadcastMessageToClient(this, "private:Неверный логин или пароль.");
                }
            }
            case "logout" -> {
                logger.info("Значение в user в case logout: {}", Arrays.toString(requestFromUser));
                clientRepository.updateEntity(this.client);
                removeClient();
                if(!this.client.getStatus().equals("Невидимка")){
                    broadcastMessageToClients(clients, "global:Пользователь " + this.client.getName() + " покинул чат.");
                }
                closeEverything(socket, bufferedWriter, bufferedReader, sessionFactory);
            }
            case "global" -> {
                logger.info("Получено сообщение от клиента с тегом: {}, текст: {}", requestFromUser[0], requestFromUser[2]);

                logger.info("Объект client до получения из БД: {}", client);
                client = clientRepository.getEntityById(Long.valueOf(requestFromUser[1]));
                logger.info("Объект client после получения из БД: {}", client);
                List<GlobalChat> globalChat = client.getGlobalChats();

                if(globalChat != null){
                    GlobalMessage globalMessage = new GlobalMessage();

                    String messageForClients = requestFromUser[0] + ":" + client.getName() + ": " + requestFromUser[2] + "\n";
                    for(GlobalChat chat : globalChat){
                        globalMessage.setGlobalChat(chat);
                        globalMessage.setMessage(requestFromUser[2]);
                        broadcastMessageToClients(clients, messageForClients);
                    }
                    // Вот так вот просто была исправлена ошибка: "The given object has a null identifier: server.model.GlobalMessage"
                    // Оказывается надо каждый раз сохранять новый объект globalMessage потому что это одно сообщение.
                    globalMessageRepository.saveEntity(globalMessage);
                }
            }
            case "private" -> {
                logger.info("Вошли в блок private.");
                // Получаем объект PrivateChat по clientId
                client = clientRepository.getEntityById(Long.valueOf(requestFromUser[1]));
                PrivateChat privateChat = privateChatRepository.getChatByClient(client);
                logger.info("Получаем объект privateChat из БД: {}", privateChat);
                Client client = clientRepository.getEntityById(Long.valueOf(requestFromUser[1]));
                logger.info("Получаем клиента из БД: {}", client);

                if (privateChat != null) {
                    // Создаем новое сообщение
                    PrivateMessage newMessage = new PrivateMessage();
                    newMessage.setPrivateChat(privateChat);  // Устанавливаем связь с чатом
                    newMessage.setMessage(requestFromUser[2]);   // Устанавливаем текст сообщения
                    // Необходимо доработать систему приватных сообщений, потому что отправить  могу только если клиент в сети и если он не невидимка
                    // Так же данный метод отправляет только ClientManager, а не Client, так что тут надо подумать.
                    //broadcastMessageToClient(client, user[3]);

                    // Сохраняем новое сообщение
                    privateMessageRepository.saveEntity(newMessage);
                } else {
                    // Обработка случая, если чат не найден
                    System.out.println("Чат не найден.");
                }
            }
            case "status" -> {
                Client client = clientRepository.getEntityById(Long.valueOf(requestFromUser[1]));
                client.setStatus(requestFromUser[2]);
                clientRepository.updateEntity(client);
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

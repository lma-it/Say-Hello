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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class ClientManager implements Runnable{
    private final Logger logger = (Logger) LoggerFactory.getLogger(ClientManager.class);
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ClientRepositoryImpl clientRepository;
    private PrivateChatRepositoryImpl privateChatRepository;
    private PrivateMessageRepositoryImpl privateMessageRepository;
    private GlobalMessageRepositoryImpl globalMessageRepository;
    private static int COUNT = 0;
    @Getter
    private Client client;
    private final SessionFactory sessionFactory;
    private final GlobalChat globalChat;

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

    public ClientManager(GlobalChat globalChat, Socket socket){
        this.globalChat = globalChat;
        this.socket = socket;
        sessionFactory = SessionService.getSessionFactory(logger, "ClientManager");
        Session session = sessionFactory.openSession();
        COUNT++;
        try{
            logger.info("Состояние session: {}", session.getStatistics());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clientRepository = new ClientRepositoryImpl(session);
            privateChatRepository = new PrivateChatRepositoryImpl(session);
            privateMessageRepository = new PrivateMessageRepositoryImpl(session);
            globalMessageRepository = new GlobalMessageRepositoryImpl(session);
        }catch (IOException e){
            catchErrors(e, "Constructor of ClientManager");
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
                catchErrors(e, "ClientManager.run");
                closeEverything(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    private void closeEverything(Object... args){
        for(Object obj : args){
            if(obj instanceof AutoCloseable){
                try{
                    ((Closeable) obj).close();
                }catch (IOException e){
                    catchErrors(e, "ClientManager.closeEverything");
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
            catchErrors(e, "ClientManager.broadcastMessageToClient");
        }
    }


    private void broadcastMessageToClients(List<ClientManager> clients, String message){
        try{
            for(ClientManager client : clients){
                client.bufferedWriter.write(message);
                client.bufferedWriter.newLine();
                client.bufferedWriter.flush();
            }
        }catch (IOException e){
            catchErrors(e, "ClientManager.broadcastMessageToClients");
        }
    }

    private void broadcastPrivateMessageToClients(List<ClientManager> clients, String message){
        try{
            for(ClientManager client : clients) {
                client.bufferedWriter.write(message);
                client.bufferedWriter.newLine();
                client.bufferedWriter.flush();
            }
        }catch (IOException e){
            catchErrors(e, "ClientManager.broadcastMessageToClients");
        }
    }


    // Удалил synchronized, может потребуется вернуть обратно, но не знаю зачем.
    private void messageFromClient(String message){
        String[] requestFromUser = message.split(":");
        logger.info("Данные о user: {}", Arrays.toString(requestFromUser));
        switch (requestFromUser[0]) {
            case "logins" -> {
                logger.info("Значение в user в case logins: {}", Arrays.toString(requestFromUser));
                StringBuilder sb = new StringBuilder();
                sb.append("logins").append(":");
                clientRepository.getAllEntities().forEach(client -> sb.append(client.getLogin()).append(":"));
                logger.info("Список всех логинов из БД: {}", sb);
                broadcastMessageToClient(this, sb.toString());
            }
            case "authorization" -> {
                logger.info("Значение в user в case authorization: {}", Arrays.toString(requestFromUser));
                requestFromUser = message.split(":");
                login = requestFromUser[1];
                userName = requestFromUser[2];
                password = requestFromUser[3];
                status = requestFromUser[4];
                client = new Client(login, userName, password, status, globalChat);
                PrivateChat privateChat = new PrivateChat();
                privateChatRepository.saveEntity(privateChat);
                client.setPrivateChat(privateChat);
                client.getPrivateChat().setClient(client);
                logger.debug("Сохраняем нового клиента в БД: {}", client.toString());
                clientRepository.saveEntity(client);
                client = clientRepository.getEntityByLoginAndPassword(login, password);
                clients.add(this);
                System.out.println("Новый клиент " + userName + " подключился к чату.");
                if(globalChat != null){
                    String afterAuthorizationMessageHistory = receiveMessageFromGlobalChat(globalMessageRepository.getAllMessages());
                    broadcastMessageToClient(this, String.format("%s\n%sВы успешно зарегистрировались. Ваш логин: %s\nИмя: %s\nПароль: %s (не сообщайте пароль никому!)", "authorization", afterAuthorizationMessageHistory, login, userName, password));
                }else{
                    broadcastMessageToClient(this, String.format("%sВы успешно зарегистрировались. Ваш логин: %s\nИмя: %s\nПароль: %s (не сообщайте пароль никому!)", "authorization", login, userName, password));
                }
            }
            case "login" -> {
                logger.info("Значение в user в case login: {}", Arrays.toString(requestFromUser));
                login = requestFromUser[1];
                password = requestFromUser[2];
                client = clientRepository.getEntityByLoginAndPassword(login, password);
                String id, name, status;
                id = String.valueOf(client.getClientId());
                name = client.getName();
                status = client.getStatus();
                this.setStatus(status);
                this.userName = client.getName();
                String clientData = "login:" + id + ":" + login + ":" + name + ":" + password + ":" + status;
                if(COUNT > clients.size()){
                    clients.add(this);
                    updateOnlineClients(clients);
                }
                logger.info("Клиенты в списке clients: {}", clients);
                broadcastMessageToClient(this, clientData);
                if (client != null) {
                    String afterLoginMessageHistory = receiveMessageFromGlobalChat(globalMessageRepository.getAllMessages());
                    logger.info("Значение в строке afterLoginMessageHistory в методе messageFromClient в блоке \"login\":\n {}", afterLoginMessageHistory);
                    broadcastMessageToClient(this, afterLoginMessageHistory);

                    String privateMessage = receiveMessageFromPrivateChats(client.getPrivateChat());
                    broadcastMessageToClient(this, privateMessage);
                } else {
                    broadcastMessageToClient(this, "global:Неверный логин или пароль.");
                }
            }
            case "logout" -> {
                COUNT--;
                logger.info("Значение в user в case logout: {}", Arrays.toString(requestFromUser));
                clientRepository.updateEntity(client);
                if(COUNT < clients.size()){
                    removeClient();
                    updateOnlineClients(clients);
                }
                if(!this.client.getStatus().equals("Невидимка")){
                    broadcastMessageToClients(clients, "global:Пользователь " + this.client.getName() + " покинул чат.");
                }
                closeEverything(socket, bufferedWriter, bufferedReader, sessionFactory);
            }
            case "global" -> {
                logger.info("Получено сообщение от клиента {} с тегом: {}, текст: {}", client.getName(), requestFromUser[0], requestFromUser[2]);

                client = clientRepository.getEntityById(Long.valueOf(requestFromUser[1]));
                logger.info("Объект client после получения из БД: {}", client);

                String messageForClients = requestFromUser[0] + ":" + LocalDate.now() + " " + client.getName() + ": " + requestFromUser[2] + "\n";

                GlobalMessage globalMessage = new GlobalMessage(globalChat, LocalDate.now() + " " + client.getName() + ": " + requestFromUser[2]);
                // globalMessage.setMessage(LocalDate.now() + " " + client.getName() + ": " + requestFromUser[2]);
                globalMessageRepository.saveEntity(globalMessage);
                broadcastMessageToClients(clients, messageForClients);
            }
            case "private" -> {

                logger.info("Вошли в блок private.");
                // client - тот кто хочет отправить сообщение, client1 - тот кому хочет client отправить сообщение.
                // Т.е запрос будет выглядеть: private:clientId1:clientId2:message. На данный момент так.
                // Почему 2, потому что в первом у нас будет передаваться тот, кто отправил личное сообщение, а 2, это тот, кому отправили.
                List<Client> privateUsers = new ArrayList<>(List.of(
                        clientRepository.getEntityById(Long.valueOf(requestFromUser[1])),
                                clientRepository.getEntityById(Long.valueOf(requestFromUser[2])
                )));
                logger.info("INFO в методе ClientManager.messageFromClient, блок private. Состояние privateUsers: {}", privateUsers);

                List<ClientManager> privateClients = new ArrayList<>();
                for (ClientManager client : clients){
                    if(client.getClient().getClientId().equals(Long.valueOf(requestFromUser[1])) || client.getClient().getClientId().equals(Long.valueOf(requestFromUser[2]))){
                        privateClients.add(client);
                    }
                }

                logger.info("INFO из метода ClientManager.messageFromClient, блок private. Состояние privateClients: {}", privateClients);
                String privateMessageForClient = requestFromUser[0] + ":" + LocalDate.now() + " " + privateUsers.getFirst().getName() + ": " + requestFromUser[3] + "\n";
                for(Client user : privateUsers){
                    PrivateMessage privateMessage = new PrivateMessage();
                    privateMessage.setPrivateChat(user.getPrivateChat());
                    privateMessage.setMessage(privateMessageForClient);
                    privateMessageRepository.saveEntity(privateMessage);
                }
                broadcastPrivateMessageToClients(privateClients, privateMessageForClient);
            }
            case "status" -> {
                Client client = clientRepository.getEntityById(Long.valueOf(requestFromUser[1]));
                client.setStatus(requestFromUser[2]);
                clientRepository.updateEntity(client);
                updateOnlineClients(clients);
            }
        }
    }



    private void updateOnlineClients(List<ClientManager> clients){
        StringBuilder clientsOnline = new StringBuilder();
        clientsOnline.append("update").append(":");
        List<ClientManager> invincible = clients.stream().filter(clientManager -> clientManager.getClient().getStatus().equals("Невидимка")).toList();
        List<ClientManager> online = clients.stream().filter(clientManager -> clientManager.getClient().getStatus().equals("В сети") || clientManager.getClient().getStatus().equals("Занят")).toList();

        if(!online.isEmpty()){
            for(ClientManager clientManager : online){
                clientsOnline.append(clientManager.client.getClientId()).append("|");
                clientsOnline.append(clientManager.client.getName()).append("|");
                clientsOnline.append(clientManager.client.getStatus()).append(":");
            }
            broadcastMessageToClients(online, clientsOnline.toString());
        }
        broadcastPrivateMessageToClients(invincible, clientsOnline.toString());
        logger.info("Содержимое в поле clientsOnline: {}", clientsOnline);
    }

    private String receiveMessageFromPrivateChats(PrivateChat privateChat){
        StringBuilder sb = new StringBuilder();
        if(privateChat.getMessages() != null){
            for(PrivateMessage privateMessage : privateChat.getMessages()){
                sb.append("private:").append(privateMessage.getMessage()).append("\n");
            }
            return sb.toString();
        }
        return "Сообщений еще нет.";
    }

    private String receiveMessageFromPrivateChats(List<PrivateMessage> privateMessages){
        StringBuilder sb = new StringBuilder();
        for(PrivateMessage message : privateMessages){
            sb.append("private:").append(message.getMessage()).append("\n");
        }
        return sb.toString();
    }


    private String receiveMessageFromGlobalChat(List<GlobalMessage> globalMessages){
        StringBuilder sb = new StringBuilder();
//        for(GlobalMessage message : globalMessages){
//            sb.append("global:").append(message.getMessage()).append("\n");
//        }
        globalMessages.forEach(message -> sb.append("global:").append(message.getMessage()).append("\n"));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ClientManager{" +
                "local port=" + socket.getLocalPort() +
                '}';
    }



    private void catchErrors(Exception e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.error("ERROR: в методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }
}

package org.client.view;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.client.abstraction.GUI;
import org.client.client.Client;
import org.client.util.Status;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

@Slf4j
public class ClientView extends GUI {
    private final Logger logger = (Logger) LoggerFactory.getLogger(ClientView.class);
    private final Socket socket;
    private JComboBox<String> statusBox;
    private JTextField login;
    private JTextField password;
    private String[] logins;
    private static String messageFromMethods;

    private JTextArea assertPanel;
    private JTextField newLogin;
    private JTextField newName;
    private JTextField newPassword;
    private JButton createNewAccount;
    private JTabbedPane pane;
    private JScrollPane globalPane;
    private JScrollPane privatePane;
    private DefaultListModel<Client> usersListModel;
    private JList<Client> onlineClients;
    private JPopupMenu popupMenu;
    private JMenuItem sendPrivateMessage;

    private JButton btnLogin;
    private JButton btnNewClient;
    private JButton sendMessages;


    private JButton btnLogout;
    private JTextArea globalMessageHistory;
    private JTextArea privateMessageHistory;
    private JTextField messageField;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Client client = new Client();
    private JDialog authorization;

    private StringBuilder globalChat;
    private StringBuilder privateChat;
    private StringBuilder privateMessage;

    // flags for sendMessage
    private boolean isAuthorized = false;
    private boolean isMsgWrited = false;
    private boolean isTryToLogin = false;
    private boolean isTryToLogout = false;
    private boolean isStatusChanged = false;
    private boolean isGetLogins = false;



    public ClientView(Socket socket){
        this.socket = socket;
        logger.info("Данные о сокете из конструктора: isConnected: {}, Порт: {}", socket.isConnected(), socket.getPort());
        try{
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            globalChat = new StringBuilder();
            privateChat = new StringBuilder();
        }catch (IOException e){
            catchErrors(e, "Constructor of ClientView");
        }

        initConstants(new Random().nextInt(300), new Random().nextInt(500), 700, 500);

        setBounds(getPOS_X(), getPOS_Y(), getWIDTH(), getHEIGHT());

        add(createHeaderOfWindow(), BorderLayout.NORTH);
        add(createBodyOfWindow(), BorderLayout.CENTER);
        add(createFooterOfWindow(), BorderLayout.SOUTH);
        setVisible(true);
    }



    @Override
    public Component createHeaderOfWindow() {
        GUIBuilder builder = new GUIBuilder();
        String[] statuses = { Status.ONLINE.getStatus(), Status.BUSY.getStatus(), Status.INVISIBLE.getStatus(), Status.OFFLINE.getStatus() };

        JTextField ipAddress = new JTextField(Arrays.toString(socket.getInetAddress().getAddress()).replace("[", "").replace("]",""));
        JTextField port = new JTextField();
        port.setText(String.valueOf(socket.getPort()));
        statusBox = new JComboBox<>(statuses);
        statusBox.setEnabled(false);
        login = new JTextField();
        password = new JTextField();
        btnLogin = new JButton("Войти");
        btnLogout = new JButton("Выйти");
        btnNewClient = new JButton("Создать аккаунт.");
        btnLogout.setVisible(false);

        return builder
                .setLayout(new GridBagLayout())
                .addComponent(ipAddress, 0, 0, 0.3f, 1)
                .addComponent(port, 1, 0, 0.3f, 1)
                .addComponent(statusBox, l -> setStatus(), 2, 0, 0.3f, 1)
                .addComponent(login, 0,1, 0.3f, 1)
                .addComponent(password, 1,1, 0.3f, 1)
                .addComponent(btnLogin, _ -> logIn(), 2,1, 0.3f, 1)
                .addComponent(btnLogout, _ -> logOut(), 2, 1, 0.3f, 1)
                .addComponent(btnNewClient, _ -> createNewAccount(), 3, 1, 0.3f, 1)
                .build();
    }


    private void setStatus(){
        messageFromMethods = "";
        String state = (String) statusBox.getSelectedItem();
        logger.info("Статус на данный момент: {}", state);
        switch (state){
            case "В сети" -> {
                removeClient(client.getId());
                addClient(client.getId(), client.getName());
                statusBox.setBackground(Color.GREEN);
                statusBox.setActionCommand("В сети");
                logger.info("Статус в поле В сети: {}", statusBox.getActionCommand());
            }
            case "Занят" -> {
                removeClient(client.getId());
                addClient(client.getId(), client.getName());
                statusBox.setBackground(Color.YELLOW);
                statusBox.setActionCommand("Занят");
                logger.info("Статус в поле Занят: {}", statusBox.getActionCommand());
            }
            case "Невидимка" -> {
                removeClient(client.getId());
                statusBox.setBackground(Color.GRAY);
                statusBox.setActionCommand("Невидимка");
                logger.info("Статус в поле Невидимка: {}", statusBox.getActionCommand());
            }
        }
        messageFromMethods = "status:" + this.client.getId() + ":" + statusBox.getActionCommand();
        isStatusChanged = true;
    }


    @Override
    public Component createBodyOfWindow() {
        GUIBuilder builder = new GUIBuilder();
        usersListModel = new DefaultListModel<>();
        onlineClients = new JList<>(usersListModel);
        onlineClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupMenu = new JPopupMenu();
        sendPrivateMessage = new JMenuItem("Написать личное сообщение");
        popupMenu.add(sendPrivateMessage);
        onlineClients.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e) && onlineClients.locationToIndex(e.getPoint()) != -1){
                    onlineClients.setSelectedIndex(onlineClients.locationToIndex(e.getPoint()));
                    popupMenu.show(onlineClients, e.getX(), e.getY());
                }
            }
        });


        sendPrivateMessage.addActionListener(_ -> sendPrivateMessage());
        pane = new JTabbedPane();
        globalMessageHistory = new JTextArea();
        globalMessageHistory.setEditable(false);
        globalPane = new JScrollPane(globalMessageHistory);
        pane.add("Общий чат", globalPane);
        pane.setSelectedComponent(globalPane);
        privateMessageHistory = new JTextArea();
        privateMessageHistory.setEditable(false);
        privatePane = new JScrollPane(privateMessageHistory);
        pane.add("Личные сообщения", privatePane);
        return builder
                .addComponent(pane, 0, 0, 0.7f, 1.0f)
                .addComponent(onlineClients, 1, 0, 0.3f, 1.0f)
                .build();
    }


    private void  sendPrivateMessage() {
        privateMessage = new StringBuilder();
        Client client = onlineClients.getSelectedValue();
        logger.info("INFO Из метода sendPrivateMessage. Данные о клиенте: {}", client);
        Long id = client.getId();
        pane.setSelectedComponent(privatePane);
        privateMessage.append(id).append(":");
        logger.info("INFO Из метода sendPrivateMessage. Информация в privateMessage: {}", privateMessage);
    }


    public void addClient(Long id, String name) {
        Client client = new Client(id, name);
        if (!usersListModel.contains(client)) {
            usersListModel.addElement(client);
        }
    }

    public void removeClient(Long id) {
        for (int i = 0; i < usersListModel.getSize(); i++) {
            if (usersListModel.getElementAt(i).getId().equals(id)) {
                usersListModel.removeElementAt(i);
                break;
            }
        }
    }


    private JDialog createRegistrationPanel(){
        JDialog registration = new JDialog();
        registration.setLayout(new GridBagLayout());

        registration.setSize(400, 300);
        registration.setPreferredSize(new Dimension(400, 300));
        createNewAccount = new JButton("Зарегистрироваться");
        assertPanel = new JTextArea("Логин менее 6 символов.\nИли такой логин уже существует.");
        assertPanel.setPreferredSize(new Dimension(300, 50));
        assertPanel.setFocusable(false);
        newLogin = new JTextField("Введите логин.", 20);
        newLogin.setPreferredSize(new Dimension(300, 30));
        mistText(newLogin, "Введите логин.");
        newName = new JTextField("Введите имя.", 20);
        newName.setPreferredSize(new Dimension(300, 30));
        mistText(newName, "Введите имя.");
        newPassword = new JTextField("Введите пароль.", 20);
        newPassword.setPreferredSize(new Dimension(300, 30));
        mistText(newPassword, "Введите пароль.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;

        registration.add(assertPanel, gbc);

        gbc.gridy = 1;
        registration.add(newLogin, gbc);

        gbc.gridy = 2;
        registration.add(newName, gbc);

        gbc.gridy = 3;
        registration.add(newPassword, gbc);

        gbc.gridy = 4;
        createNewAccount.addActionListener(_ -> authorization());
        createNewAccount.setEnabled(false);
        registration.add(createNewAccount, gbc);
        registration.setVisible(false);
        statusBox.setEditable(false);
        return registration;
    }


    private void mistText(JTextField textField, String textForMist){
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(textField.getText().equals(textForMist)){
                    textField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(textField.getText().isEmpty()){
                    textField.setText(textForMist);
                }
            }
        });
    }




    @Override
    public Component createFooterOfWindow() {
        messageField = new JTextField();
        sendMessages = new JButton("Send");
        GUIBuilder builder = new GUIBuilder();

        return builder
                .setLayout(new GridBagLayout())
                .addComponent(messageField, _ -> writeMessage(), 0, 0, 0.8f, 1)
                .addComponent(sendMessages, _ -> writeMessage(), 1, 0, 0.2f, 1)
                .build();
    }




    private void authorization(){
        statusBox.setEnabled(false);
        messageFromMethods = "";
        authorization.setVisible(false);
        StringBuilder authorization = new StringBuilder("authorization").append(":");
        authorization.append(newLogin.getText()).append(":");
        authorization.append(newName.getText()).append(":");
        authorization.append(newPassword.getText()).append(":");
        authorization.append(client.getStatus());
        logger.info("Лог из метода authorization. Информация от новом пользователе: {}", authorization);
        client = new Client(newLogin.getText(), newName.getText(), newPassword.getText());
        messageFromMethods = authorization.toString();
        isAuthorized = true;
    }

    private void logIn() {
        messageFromMethods = "";
        StringBuilder logIn = new StringBuilder("login:")
                .append(login.getText())
                        .append(":")
                                .append(password.getText());
        logger.info("Данные успешно отправлены: {}", logIn);
        btnNewClient.setVisible(false);
        btnLogin.setVisible(false);
        btnLogout.setVisible(true);
        statusBox.setEnabled(true);
        messageFromMethods = logIn.toString();
        isTryToLogin = true;
    }

    private void logOut() {
        messageFromMethods = "";
        btnNewClient.setVisible(true);
        btnLogout.setVisible(false);
        btnLogin.setVisible(true);
        ActionListener[] listeners = statusBox.getActionListeners();
        for(ActionListener listener : listeners){
            statusBox.removeActionListener(listener);
        }
        statusBox.setSelectedItem("Не в сети");
        statusBox.setBackground(Color.RED);
        login.setText("");
        password.setText("");
        globalMessageHistory.setText("");
        privateMessageHistory.setText("");
        statusBox.setEnabled(false);
        usersListModel.clear();
        // Доработать этот метод, потому что он будет изменять состояние клиента и возможно клиент будет логиниться со статусом "Не в сети"
        messageFromMethods = "logout";
        isTryToLogout = true;
    }

    private boolean isCorrect(String[] logins){
        new Thread(() -> {
            while (true){
                if(logins != null){
                    while (Arrays.stream(logins).anyMatch(login -> login.equals(newLogin.getText()) && newLogin.getText().length() < 6)){
                        assertPanel.setVisible(true);
                    }
                    break;
                }else if(newLogin.getText().length() < 6){
                    assertPanel.setVisible(true);
                }
            }
        }).start();
        assertPanel.setVisible(false);
        return true;
    }

    void createNewAccount(){
        isGetLogins = true;
        authorization = createRegistrationPanel();
        logger.info("Вошли в метод создания нового аккаунта.");
        logger.info("Текущая видимость authorization:{}", authorization.isVisible());
        authorization.setLocationRelativeTo(this);
        authorization.setVisible(true);
        logger.info("Выставляем видимость поля authorization в значение true: {}", authorization.isVisible());


        if(isCorrect(logins) && newName != null && !newName.getText().equals("Введите имя.") && newPassword != null && !newPassword.getText().equals("Введите пароль.")){
            logger.info("В случае если logins != null, то состояние активности кнопки createNewAccount: {}", createNewAccount.isEnabled());
            createNewAccount.setEnabled(true);
        }
        new Thread(() -> {
            while (true){
                if(newLogin.isFocusOwner() || newLogin.getText().equals("Введите логин.") || newName.isFocusOwner() || newName.getText().equals("Введите имя.") || newPassword.isFocusOwner() || newPassword.getText().equals("Введите пароль."))
                    createNewAccount.setEnabled(false);
                else {
                    createNewAccount.setEnabled(true);
                    break;
                }
            }
        }).start();
    }


    public void listenForMessage(){
        new Thread(() -> {
            String message;
            while(socket.isConnected()){
                try{
                    message = bufferedReader.readLine();
                    if(message != null){
                        logger.info("Лог из метода listenForMessage. Текст в message: {}", message);
                        receiveFromServer(message);
                    }
                }catch (IOException e){
                    catchErrors(e, "listenForMessage");
                    closeEverything(socket, bufferedWriter, bufferedReader);
                    break;
                }
            }
        }).start();
    }



    private void closeEverything(Object... args){
        for(Object obj : args){
            if(obj instanceof AutoCloseable){
                try{
                    ((Closeable) obj).close();
                }catch (IOException e){
                    catchErrors(e, "closeEverything");
                }
            }
        }
    }

    private void writeMessage(){
        messageFromMethods = "";
        StringBuilder message = new StringBuilder();
        if(pane.getSelectedIndex() == 0){
            message.append("global").append(":");
            message.append(this.client.getId()).append(":");
            message.append(messageField.getText()).append("\n");
            logger.info("Отправка сообщения на сервер с тегом global: {}", message);
        }else{
            message.append("private").append(":");
            message.append(this.client.getId()).append(":");
            message.append(privateMessage.toString());
            message.append(messageField.getText()).append("\n");
            logger.info("Отправка сообщения на сервер с тегом private: {}", message);
        }
        messageFromMethods = message.toString();
        isMsgWrited = true;
    }


    public void sendMessage(){
        try{

            while (socket.isConnected()){
                String message = "";

                if(isGetLogins){
                    message = "logins";
                    isGetLogins = false;
                }
                if(isAuthorized){
                    message = messageFromMethods;
                    messageFromMethods = "";
                    isAuthorized = false;
                }
                if(isStatusChanged){
                    message = messageFromMethods;
                    messageFromMethods = "";
                    isStatusChanged = false;
                }
                if(isTryToLogin){
                    message = messageFromMethods;
                    messageFromMethods = "";
                    isTryToLogin = false;
                }
                if(isTryToLogout){
                    message = messageFromMethods;
                    messageFromMethods = "";
                    isTryToLogout = false;
                }
                if(isMsgWrited){
                    message = messageFromMethods;
                    messageFromMethods = "";
                    isMsgWrited = false;
                    messageField.setText("");
                }

                if(!message.isEmpty()){
                    bufferedWriter.write(message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        }catch (IOException e){catchErrors(e, "sendMessage");
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }




    private void showMessage(String chat, String message){
        logger.info("INFO из метода showMessage. Содержание в message: {}", message);
        if(chat.equals("global")){
            globalChat.append(message).append("\n");
            globalMessageHistory.setText(globalChat.toString());
        }else{
            logger.info("Состояние message в блоке private в методе showMessage: {}", message);
            privateChat.append(message).append("\n");
            privateMessageHistory.setText(privateChat.toString());
        }
    }




    private void receiveFromServer(String messageFromServer){
        if(messageFromServer.contains("logins:")){
            String allLogins = messageFromServer.replace("logins:", "");
            logins = allLogins.split(":");
            logger.info("Все логины из Базы Данных: {}", Arrays.toString(logins));
        } else if (messageFromServer.contains("authorization:")) {
            logger.info("Состояние в строке messageFromServer в блоке authorization: {}", messageFromServer);
            String newClient = messageFromServer.replace("authorization:", "");
            showMessage("global", newClient);
        } else if(messageFromServer.contains("global:")){
            String chatType = "global";
            String message = messageFromServer.replace("global:","");
            showMessage(chatType, message);
        } else if(messageFromServer.contains("private:")){
            String chatType = "private";
            logger.info("INFO из метода receiveFromServer в блоке private. Состояние messageFromServer: {}",  messageFromServer);
            String message = messageFromServer.replace("private:","");
            logger.info("INFO из метода receiveFromServer в блоке private. Состояние message: {}",  message);
            showMessage(chatType, message);
        } else if(messageFromServer.contains("login:")){
            String[] clientData = messageFromServer.split(":");
            client.setId(Long.valueOf(clientData[1]));
            client.setLogin(clientData[2]);
            client.setName(clientData[3]);
            client.setPassword(clientData[4]);
            client.setStatus(clientData[5]);
            statusBox.setSelectedItem(client.getStatus());
            logger.info("Вошел новый клиент: {}", client.toString());
        }else if(messageFromServer.contains("update:")){
            usersListModel.clear();
            String[] clientsOnline = messageFromServer.replace("update:", "").split(":");
            for(int i = 0; i < clientsOnline.length; i++){
                String[] temp = clientsOnline[i].split("\\|");
                if(!(temp.length <=2) && !temp[2].equals("Невидимка")){
                    addClient(Long.valueOf(temp[0]), temp[1]);
                    logger.info("Значение в temp[0]: {}, значение в temp[1]: {}, значение в temp[2]: {} значение i: {}", temp[0], temp[1], temp[2], i);
                }
            }
        }
    }



    private void hideHeaderFields(){
        login.setOpaque(false);
        login.setBackground(new Color(255, 255, 255, 100));
        login.setForeground(Color.BLACK);
        login.setFocusable(false);
        login.setEditable(false);
        password.setOpaque(false);
        password.setBackground(new Color(255, 255, 255, 100));
        password.setText("********");
        password.setForeground(Color.BLACK);
        password.setFocusable(false);
        password.setEditable(false);
        btnLogin.setVisible(false);
        btnLogout.setVisible(true);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if(e.getID() == WindowEvent.WINDOW_CLOSING){
            closeEverything(socket, bufferedWriter, bufferedReader);
        }else{
            this.repaint();
        }
    }


    private void catchErrors(Exception e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.error("ERROR: В методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }


}

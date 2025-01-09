package org.client.view;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.client.abstraction.GUI;
import org.client.client.Client;
import org.client.util.Status;
import org.jetbrains.annotations.NotNull;
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
    private JComboBox<String> status;
    private JTextField login;
    private JTextField password;
    private String[] logins;

    private JTextArea assertPanel;
    private JTextField newLogin;
    private JTextField newName;
    private JTextField newPassword;
    private JButton createNewAccount;

    private JButton btnLogin;
    private JButton btnNewClient;

    private JButton btnLogout;
    private JTextArea messageHistory;
    private JTextField messageField;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final Client client = new Client();
    private final JDialog authorization = createRegistrationPanel();

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
        JTextField port = new JTextField(socket.getLocalPort());
        status = new JComboBox<>(statuses);
        status.setActionCommand(Status.OFFLINE.getStatus());
        status.setBackground(Color.RED);
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
                .addComponent(status, l -> setStatus(), 2, 0, 0.3f, 1)
                .addComponent(login, 0,1, 0.3f, 1)
                .addComponent(password, 1,1, 0.3f, 1)
                .addComponent(btnLogin, _ -> logIn(), 2,1, 0.3f, 1)
                .addComponent(btnLogout, _ -> logOut(), 2, 1, 0.3f, 1)
                .addComponent(btnNewClient, _ -> createNewAccount(), 3, 1, 0.3f, 1)
                .build();
    }


    private String setStatus(){
        String state = (String) status.getSelectedItem();
        logger.info("Статус на данный момент: {}", state);
        switch (state){
            case "В сети" -> {
                status.setBackground(Color.GREEN);
                status.setActionCommand("В сети");
                logger.info("Статус в поле В сети: {}", status.getActionCommand());
            }
            case "Занят" -> {
                status.setBackground(Color.YELLOW);
                status.setActionCommand("Занят");
                logger.info("Статус в поле Занят: {}", status.getActionCommand());
            }
            case "Невидимка" -> {
                status.setBackground(Color.GRAY);
                status.setActionCommand("Невидимка");
                logger.info("Статус в поле Невидимка: {}", status.getActionCommand());
            }
            case "Не в сети" -> {
                status.setBackground(Color.RED);
                status.setActionCommand("Не в сети");
                logger.info("Статус в поле Не в сети: {}", status.getActionCommand());
            }
        }
        return status.getSelectedItem().toString();
    }

    @Override
    public Component createBodyOfWindow() {
        GUIBuilder builder = new GUIBuilder();
        messageHistory = new JTextArea();
        return builder
                .addComponent(messageHistory, 0, 0, 1.0f, 1.0f)
                .build();
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
        GUIBuilder builder = new GUIBuilder();

        return builder
                .setLayout(new GridBagLayout())
                .addComponent(messageField, _ -> writeMessage(), 0, 0, 0.8f, 1)
                .addComponent(new JButton("Send"), _ -> writeMessage(), 1, 0, 0.2f, 1)
                .build();
    }




    private String authorization(){
        authorization.setVisible(false);
        String[] newClient = new String[9];
        newClient[0] = "authorization";
        newClient[1] = ":";
        newClient[2] = newLogin.getText();
        newClient[3] = ":";
        newClient[4] = newName.getText();
        newClient[5] = ":";
        newClient[6] = newPassword.getText();
        newClient[7] = ":";
        newClient[8] = client.getStatus();
        logger.info("Лог из метода authorization. Информация от новом пользователе: {}", Arrays.toString(newClient));
        return Arrays.toString(newClient);
    }

    private String logIn() {
        StringBuilder logIn = new StringBuilder("login:")
                .append(login.getText())
                        .append(":")
                                .append(password.getText());
        logger.info("Считаны данные из поля login: {}", login.getText());
        logger.info("Считаны данные из поля password: {}", password.getText());
        logger.info("Попытка отправить данные на сервер.");
        logger.info("Данные успешно отправлены: {}", logIn);
        return logIn.toString();
    }

    private String logOut() {
        return "logout";
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
        logger.info("Вошли в метод создания нового аккаунта.");
        logger.info("Текущая видимость authorization:{}", authorization.isVisible());
        authorization.setLocationRelativeTo(this);
        authorization.setVisible(true);
        logger.info("Выставляем видимость поля authorization в значение true: {}", authorization.isVisible());

        isGetLogins = true;
        logger.info("Данные отправлены, получение logins.");
        logger.info("Данные строки login: {}", login.getText());

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

    private String writeMessage(){
        String message = messageField.getText();
        if(!message.isEmpty()){
            messageField.setText("");
            isMsgWrited = true;
            return message;
        }
        return message;
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
                    message = authorization();
                    isAuthorized = false;
                }
                if(isStatusChanged){
                    message = setStatus();
                    isStatusChanged = false;
                }
                if(isTryToLogin){
                    message = logIn();
                    isTryToLogin = false;
                }
                if(isTryToLogout){
                    message = logOut();
                    isTryToLogout = false;
                }
                if(isMsgWrited){
                    message = writeMessage();
                    isMsgWrited = false;
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




    private void receiveFromServer(String messageFromServer){
        String[] message = messageFromServer.split(":");
        switch (message[0]){
            case "logins" -> {
                String allLogins = messageFromServer.replace("logins:", "");
                logins = allLogins.split(":");
                logger.info("Все логины из Базы Данных: {}", Arrays.toString(logins));
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

    private void repaintFrame(JFrame frame){
        frame.invalidate();
        frame.revalidate();
        frame.repaint();
    }

    private void catchErrors(Exception e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.error("ERROR: В методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }


}

//package org.client.view;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.client.abstraction.GUI;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.FocusEvent;
//import java.awt.event.FocusListener;
//
//@Getter
//@Setter
//public class RegistrationWindow extends GUI {
//
//
//
//    public JFrame createAuthorizedWindow(){
//        JFrame registration = new JFrame("Регистрация");
//        registration.setSize(400, 300);
//        registration.setPreferredSize(new Dimension(400, 300));
//        registration.setLayout(new GridBagLayout());
//        createNewAccount = new JButton("Зарегистрироваться");
//        assertPanel = new JTextArea("Логин менее 6 символов, или такой логин уже существует.");
//        assertPanel.setPreferredSize(new Dimension(300, 50));
//        assertPanel.setFocusable(false);
//        newLogin = new JTextField("login", 20);
//        newLogin.setPreferredSize(new Dimension(300, 30));
//        mistText(newLogin, "Введите логин.");
//        newName = new JTextField("michael", 20);
//        newName.setPreferredSize(new Dimension(300, 30));
//        mistText(newName, "Введите имя.");
//        newPassword = new JTextField("12334", 20);
//        newPassword.setPreferredSize(new Dimension(300, 30));
//        mistText(newPassword, "Введите пароль.");
//        registration.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.insets = new Insets(10, 10, 10, 10);
//
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//
//        registration.add(assertPanel, gbc);
//
//        gbc.gridy = 1;
//        registration.add(newLogin, gbc);
//
//        gbc.gridy = 2;
//        registration.add(newName, gbc);
//
//        gbc.gridy = 3;
//        registration.add(newPassword, gbc);
//
//        gbc.gridy = 4;
//        createNewAccount.setEnabled(false);
//        registration.add(createNewAccount, gbc);
//
//        registration.setLocationRelativeTo(this);
//        registration.setVisible(true);
//        registration.revalidate();
//        registration.repaint();
//        return registration;
//    }
//
//
//}

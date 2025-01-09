package server.util;


import ch.qos.logback.classic.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import server.model.Client;
import server.model.GlobalChat;
import server.model.PrivateChat;


public class SessionService {

    public static Session getSession(Logger logger){
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Client.class);
        configuration.addAnnotatedClass(GlobalChat.class);
        configuration.addAnnotatedClass(PrivateChat.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        logger.info("Состояние sessionFactory: {}", sessionFactory.getStatistics().toString());
        return sessionFactory.openSession();
    }

}
